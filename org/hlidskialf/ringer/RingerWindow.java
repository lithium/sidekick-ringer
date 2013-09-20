// This code is Public Domain
// Written by Matthew Wiggins, 2007
// No rights reserved

package org.hlidskialf.ringer;

import danger.util.DEBUG;

import danger.app.Application;
import danger.app.ResourceDatabase;
import danger.app.Event;
import danger.storage.StorageManager;
import danger.app.SettingsDB;
import danger.audio.Tone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import danger.ui.ScreenWindow;
import danger.ui.DialogWindow;
import danger.ui.Menu;
import danger.ui.CheckBox;
import danger.ui.MenuItem;
import danger.ui.TextField;
import danger.ui.StaticText;
import danger.ui.AlertWindow;
import danger.ui.file.SaveFileDialog;
import danger.ui.file.OpenFileDialog;


class ToneFilenameFilter implements FilenameFilter 
{
    public boolean accept(File dir, String name)
    {
        File f = new File(dir,name);
        if (f.isDirectory()) return true;
        int i = name.lastIndexOf('.');
        String ext = name.substring(i+1).toLowerCase();
        if (ext.equals("wav") || ext.equals("mp3") || ext.equals("x-aiff") || ext.equals("mid") || ext.equals("midi") || ext.equals("mmf"))
            return true;
        return false;
    }
}

public class RingerWindow extends ScreenWindow
        implements Resources, Commands
{
    private Application pApp;
    ResourceDatabase pRDB;
    SettingsDB pSDB;

    private Tone pCurTone;
    private ToneBrowser pToneBrowser;
    private boolean pNotifyOnLoad;
    private boolean pDontDiscardDefaults;

    DialogWindow pAboutDialog;
    AlertWindow pNoStorageAlert;
    SaveFileDialog pSaveFileDialog;
    OpenFileDialog pOpenFileDialog;

    DialogWindow pRenameDialog;
    TextField pRenameText;
    StaticText pRenameStatic; 

    DialogWindow pSettingsDialog;
    CheckBox pSettingsNotifyCheck,pSettingsDontDiscardCheck;

    DialogWindow pInfoDialog;
    StaticText pInfoGroupText, pInfoNameText, pInfoProductIDText, pInfoOriginText, pInfoClassText, pInfoMimeText, pInfoLoopText;

    static final boolean DefaultNotifyOnLoad = false;
    static final boolean DefaultDontDiscardDefaults = true;


    public RingerWindow()
    {
        pApp = Application.getCurrentApp();
        pRDB = pApp.getResources();

    }

    public void onDecoded()
    {
        pToneBrowser = (ToneBrowser)getDescendantWithID(kScreenMain_ToneBrowser);

        pRenameDialog = pApp.getDialog(kDialogRename,this);
        pRenameText = (TextField)pRenameDialog.getDescendantWithID(kDialogRename_Text);
        pRenameStatic = (StaticText)pRenameDialog.getDescendantWithID(kDialogRename_Static);

        pAboutDialog = pApp.getDialog(kDialogAbout,this);

        pNoStorageAlert = pApp.getAlert(kAlertNoStorage,this);

        pSettingsDialog = pApp.getDialog(kDialogSettings,this);
        pSettingsNotifyCheck = (CheckBox)pSettingsDialog.getDescendantWithID(kDialogSettings_NotifyCheck);
        pSettingsDontDiscardCheck = (CheckBox)pSettingsDialog.getDescendantWithID(kDialogSettings_DiscardCheck);

        pInfoDialog = pApp.getDialog(kDialogToneInfo,this);
        pInfoGroupText = (StaticText)pInfoDialog.getDescendantWithID(kDialogToneInfo_Group);
        pInfoNameText = (StaticText)pInfoDialog.getDescendantWithID(kDialogToneInfo_Name);
        pInfoProductIDText = (StaticText)pInfoDialog.getDescendantWithID(kDialogToneInfo_ProductID);
        pInfoOriginText = (StaticText)pInfoDialog.getDescendantWithID(kDialogToneInfo_Origin);
        pInfoClassText = (StaticText)pInfoDialog.getDescendantWithID(kDialogToneInfo_Class);
        pInfoMimeText = (StaticText)pInfoDialog.getDescendantWithID(kDialogToneInfo_MimeType);
        pInfoLoopText = (StaticText)pInfoDialog.getDescendantWithID(kDialogToneInfo_LoopCount);

        loadSettings();
    }
    public void loadSettings()
    {
        pSDB = new SettingsDB(pRDB.getString(kString_AppName));
        try { pNotifyOnLoad = (pSDB.getIntValue("NotifyOnLoad") == 1);
        } catch (Exception e) { pNotifyOnLoad = DefaultNotifyOnLoad; }

        try { pDontDiscardDefaults = (pSDB.getIntValue("DontDiscardDefaults") == 1);
        } catch (Exception e) { pDontDiscardDefaults = DefaultDontDiscardDefaults; }


    }

    public boolean receiveEvent(Event evt)
    {
        switch (evt.type) {
            case EVENT_MENU_LOAD_TONE:
                if (!storage_is_available()) {
                    pNoStorageAlert.show();
                    return true;
                }
                pOpenFileDialog = OpenFileDialog.createAndShow(OpenFileDialog.OPEN_OPTIONS_FILTER_OUT_DOT_FILES, null, new ToneFilenameFilter(), new Event(this, EVENT_DIALOG_OPEN_FILE_OK), null, null);
                return true;
            case EVENT_DIALOG_OPEN_FILE_OK: {
                File f = (File)evt.argument;
                byte[] data = get_file_contents(f);
                if (data == null) return true;
                String name = f.getName();
                ToneBrowser.addTone(data, name, f.getName(), new Event(this, EVENT_TONE_ADD_OK), null, pNotifyOnLoad);
                return true;
            }
            case EVENT_TONE_ADD_OK: 
                pToneBrowser.refresh();
                return true;

            case EVENT_MENU_SAVE_TONE:
                pCurTone = pToneBrowser.getSelectedTone();
                pSaveFileDialog = SaveFileDialog.createAndShow(pCurTone.getName(), null, SaveFileDialog.SAVE_OPTIONS_DEFAULT, null,null, new Event(this, EVENT_DIALOG_SAVE_FILE_OK), null,null);
                return true;
            case EVENT_DIALOG_SAVE_FILE_OK:
                ToneBrowser.saveTone(pCurTone.getResourceID(), (File)evt.argument);
                return true;

            case EVENT_MENU_RENAME_TONE:
                pCurTone = pToneBrowser.getSelectedTone();
                pRenameText.setText(pCurTone.getName());
                pRenameDialog.show();
                return true;
            case EVENT_DIALOG_RENAME_OK: {
                String new_name = pRenameText.getText();
                if (!new_name.equals(pCurTone.getName())) {
                    pCurTone.setName(new_name);
                    pToneBrowser.refresh();
                }
            }

            case EVENT_MENU_DISCARD_TONE:
                pToneBrowser.discardSelectedTone();
                return true;

            case EVENT_MENU_INFO_TONE: {
                Tone t = pToneBrowser.getSelectedTone();
                pInfoGroupText.setText( t.getGroupName() );
                pInfoNameText.setText( t.getName() );
                pInfoProductIDText.setText( t.getProductID() );
                pInfoMimeText.setText( t.getMimeType() );
                /*
                pInfoOriginText.setText( t.getOrigin() );
                pInfoClassText.setText( t.getToneClass() );
                pInfoLoopText.setText( t.getLoopCount() );
                */
                pInfoDialog.show();
                return true;
            }

            case EVENT_MENU_SETTINGS:
                pSettingsNotifyCheck.setValue(pNotifyOnLoad ? 1 : 0);
                pSettingsDontDiscardCheck.setValue(pDontDiscardDefaults ? 1 : 0);
                pSettingsDialog.show();
                return true;
            case EVENT_DIALOG_SETTINGS_OK: {
                boolean notify_onload = (pSettingsNotifyCheck.getValue() == 1);
                if (notify_onload != DefaultNotifyOnLoad) pSDB.setIntValue("NotifyOnLoad", notify_onload ? 1 : 0);
                else pSDB.remove("NotifyOnLoad");
                pNotifyOnLoad = notify_onload;

                boolean dont_discard = (pSettingsDontDiscardCheck.getValue() == 1);
                if (dont_discard != DefaultDontDiscardDefaults) pSDB.setIntValue("DontDiscardDefaults", dont_discard ? 1 : 0);
                else pSDB.remove("DontDiscardDefaults");
                pDontDiscardDefaults = dont_discard;

                return true;
            }

            case EVENT_MENU_ABOUT:
                pAboutDialog.show();
                return true;

        }
        return super.receiveEvent(evt);
    }

    public boolean eventWidgetUp(int which, Event evt)
    {
        switch (which) {
            case Event.DEVICE_BUTTON_BACK:
                Application.getCurrentApp().returnToLauncher();
                return true;
        }
        return super.eventWidgetUp(which, evt);
    }

    public boolean eventWidgetDown(int which, Event evt)
    {        
        return super.eventWidgetDown(which, evt);
    }

    public void adjustActionMenuState(Menu hwMenu) {
        hwMenu.removeAllItems();
        hwMenu.addFromResource(pRDB, kMenuAction, this);

        if ((!pToneBrowser.isImportedSelected()) && pDontDiscardDefaults) {
            hwMenu.getItemWithID(kMenuAction_Discard).disable();
        }
    }


    private boolean storage_is_available() {
        String[] paths = StorageManager.getRemovablePaths();
        return (paths.length > 0);
    }

    private byte[] get_file_contents(File f)
    {
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();
            return data;
        } catch (Exception ex) {
            DEBUG.p(ex);
            return null;
        }
    }
}

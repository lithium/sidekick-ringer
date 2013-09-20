// This code is Public Domain
// Written by Matthew Wiggins, 2008
// No rights reserved

package org.hlidskialf.ringer;
import danger.util.DEBUG;
import danger.app.Application;
import danger.app.ResourceDatabase;
import danger.app.Event;
import danger.app.Listener;
import danger.ui.View;
import danger.ui.Container;
import danger.ui.PopupMenu;
import java.util.Vector;
import danger.audio.ToneClass;
import danger.audio.ToneRights;
import danger.audio.ToneGallery;
import danger.audio.Tone;
import danger.audio.ToneOrigin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ToneBrowser extends danger.ui.Container
        implements Resources, Commands
{
    private PopupMenu pPopupGroup;
    private ToneListView pToneList;

    int pImportedGroupID;

    public ToneBrowser()
    {
    }

    public boolean isImportedSelected() { return (pPopupGroup.getValue() == pImportedGroupID); }

    public void onDecoded()
    {
        int i;
        pPopupGroup = (PopupMenu)getDescendantWithID(kToneBrowser_PopupGroup);
        Vector<String> groups = ToneGallery.getGroupNames(ToneClass.ALL, ToneRights.FULL);
        pImportedGroupID = 0;
        for (i=0; i < groups.size(); i++) {
            String group = groups.get(i);
            if (group.substring(0,2).equals("##")) {
                pImportedGroupID--;
                continue;
            }
            pPopupGroup.addItem(group, EVENT_MENU_TONE_GROUP, 0, group, this);
            if (group == "Imported") pImportedGroupID += i;
        }
        pPopupGroup.resizeToFitLongestItem();
        pPopupGroup.setValue(pImportedGroupID);

        pToneList = (ToneListView)getDescendantWithID(kToneBrowser_ToneList);
    }

    public boolean receiveEvent(Event evt)
    {
        switch (evt.type) {
            case EVENT_MENU_TONE_GROUP:
                pToneList.setToneGroup( (String)evt.argument );
                pToneList.refresh();
                return true;
        }
        return super.receiveEvent(evt);
    }

    public void refresh()
    {
        pToneList.refresh();
    }


    public Tone getSelectedTone() { return ToneGallery.getToneFromID(getSelectedToneID()); }
    public int getSelectedToneID() { return pToneList.getSelectedToneID(); }

    public void discardSelectedTone() {
        ToneGallery.remove(getSelectedToneID());
        refresh();
    }
    

    public static void addTone(byte[] data, String name, String productID, Event ok_evt, Event fail_evt, boolean notify)
    {
        ToneGallery.add(ToneGallery.Type.SEQUENCE, data, name, productID, ok_evt, fail_evt, ToneOrigin.USER, ToneRights.FULL, notify);
    }
    public static boolean saveTone(int toneID, File path)
    {
        Tone tone = ToneGallery.getToneFromID(toneID);
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write( tone.getFormattedData() );
            fos.close();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

}

// This code is Public Domain
// Written by Matthew Wiggins, 2008
// No rights reserved

package org.hlidskialf.ringer;
import danger.util.DEBUG;

import danger.ui.Bitmap;
import danger.ui.View;
import danger.ui.Menu;
import danger.ui.Font;
import danger.ui.MenuItem;
import danger.ui.ListView;

import danger.app.Application;
import danger.app.ResourceDatabase;
import danger.app.Event;
import danger.app.Listener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.Comparator;
import java.lang.Integer;

import danger.audio.ToneGallery;
import danger.audio.ToneClass;
import danger.audio.ToneRights;
import danger.audio.ToneOrigin;
import danger.audio.Tone;
import danger.audio.PreviewTone;
import danger.audio.RingToneObject;


public class ToneListView extends danger.ui.ListView
        implements Resources, Commands
{
    private String pToneGroup;
    private Tone pPreviewTone;

    public ToneListView()
    {
        super();
        setFont(Font.findSystemFont(Font.F_MONOSPACE));
        pToneGroup = "Imported";
        pPreviewTone = null;
    }

    public void show()
    {
        super.show();
        refresh();
    }
    public void setToneGroup(String group) { pToneGroup = group; }
    public String getToneGroup() { return pToneGroup; }


    public boolean eventWidgetDown(int which, Event evt)
    {        
        switch (which) { 
            case Event.DEVICE_WHEEL_BUTTON:
                int id = getSelectedToneID();
                if (pPreviewTone != null && pPreviewTone.isPlaying())
                    pPreviewTone.stopPlaying();
                pPreviewTone = ToneGallery.getToneFromID(id);
                if (pPreviewTone != null) {
                    pPreviewTone.setLoopCount(1);
                    pPreviewTone.play(Tone.USE_DEFAULT_VOLUME);
                }
                return true;
            case Event.DEVICE_BUTTON_CANCEL:
                if (pPreviewTone != null && pPreviewTone.isPlaying())
                    pPreviewTone.stopPlaying();
        }
        return super.eventWidgetDown(which, evt);
    }
    
    public void refresh()
    {
        int klas = ToneClass.ALL;
        int rites = ToneRights.DEFAULT;
        String group = pToneGroup;
        int j;

        removeAllItems();
        Vector<String> names = ToneGallery.getNamesFromGroup(group,klas,rites);
        for (j=0; j < names.size(); j++) {
            String name = names.get(j);
            int id = ToneGallery.getIDFromNameAndGroup(name,group);
            Tone t = ToneGallery.getToneFromID(id);
            String type = t.getMimeType();
            if (type != null)
                type = type.substring(type.lastIndexOf('/')+1);
            else
                type = " ";
            String title = String.format("%-35s %12s", name, type);
            MenuItem mi = addItem(title);
            mi.setUserData(new Integer(id));
        }
        sort();
        invalidate();
    }

    public int getSelectedToneID()
    {
        MenuItem mi = getSelectedItem();
        Integer id = (Integer)mi.getUserData();
        return id.intValue();
    }
}

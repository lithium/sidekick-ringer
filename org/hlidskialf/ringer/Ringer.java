// This code is Public Domain
// Written by Matthew Wiggins, 2008
// No rights reserved

package org.hlidskialf.ringer;

import danger.app.Application;
import danger.ui.ScreenWindow;
import danger.storage.StorageManager;

import danger.util.DEBUG;

public class Ringer
        extends Application
        implements Resources, Commands
{
    static private RingerWindow pWindow;

    public Ringer() {
        pWindow = (RingerWindow)getResources().getScreen(kScreenMain);
        pWindow.setTitle( getString(kString_AppName) );
        pWindow.setSubTitle( getString(ID_VERSION) );

        StorageManager.registerStorageDeviceListener(pWindow);
    }

    public void resume() { pWindow.show(); }
    public void launch() { pWindow.show(); }
    public void suspend() {}

}

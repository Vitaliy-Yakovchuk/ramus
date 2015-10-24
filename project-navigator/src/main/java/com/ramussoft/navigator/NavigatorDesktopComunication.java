package com.ramussoft.navigator;

import java.io.IOException;

import com.ramussoft.local.DesktopComunication;

public abstract class NavigatorDesktopComunication extends DesktopComunication {

    public NavigatorDesktopComunication() throws IOException {
        super();
    }

    @Override
    protected String getFileTitle() {
        return "ramus-navigator";
    }

}

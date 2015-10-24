package com.ramussoft.gui.core;

import java.awt.Component;

import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;

public class TabDockable extends DefaultMultipleCDockable {

    public TabDockable(MultipleCDockableFactory<?, ?> factory,
                       Component content) {
        super(factory, content);
    }

}

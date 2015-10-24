package com.ramussoft.gui.common;

import java.awt.BorderLayout;

public abstract class AbstractUniqueView extends AbstractView implements UniqueView {

    public AbstractUniqueView(GUIFramework framework) {
        super(framework);
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.WEST;
    }

}

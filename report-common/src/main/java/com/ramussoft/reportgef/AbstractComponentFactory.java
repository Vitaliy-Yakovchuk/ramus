package com.ramussoft.reportgef;

import javax.swing.ImageIcon;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Bounds;


public abstract class AbstractComponentFactory implements ComponentFactory {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Component createComponent(Diagram diagram, Engine engine,
                                     AccessRules accessRules, Bounds bounds) {
        return getComponent(engine, accessRules, bounds);
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public String getName() {
        return getType();
    }
}

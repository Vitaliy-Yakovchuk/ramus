package com.ramussoft.reportgef;

import javax.swing.ImageIcon;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Bounds;

public interface ComponentFactory {

    String getName();

    Component getComponent(Engine engine, AccessRules accessRules, Bounds bounds);

    String getType();

    ImageIcon getIcon();

    String getTitle();

    String getDescription();

    Component createComponent(Diagram diagram, Engine engine,
                              AccessRules accessRules, Bounds bounds);

}

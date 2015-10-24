package com.ramussoft.gui.common;

import javax.swing.JComponent;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;

public interface AttributePreferenciesEditor {

    /**
     * Method creates {@link JComponent} for attribute editor.
     *
     * @param attribute Can be <code>null</code> if it is new attribute.
     * @param engine
     * @return JComponent for editing attribute properties.
     */

    JComponent createComponent(Attribute attribute, Engine engine, AccessRules accessRules);

    /**
     * Apply additional parameters to the attribute. Attribute should not be
     * <code>null</code>.
     */

    void apply(Attribute attribute, Engine engine, AccessRules accessRules);

    boolean canApply();

}

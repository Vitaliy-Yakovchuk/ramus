package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;

public class Util {

    public static Long getQualifierId(Engine engine, Element element) {
        Attribute qualifierId = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.QUALIFIER_ID);
        return (Long) engine.getAttribute(element, qualifierId);
    }

}

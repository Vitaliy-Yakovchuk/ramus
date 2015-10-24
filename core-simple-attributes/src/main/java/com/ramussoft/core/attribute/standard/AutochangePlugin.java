package com.ramussoft.core.attribute.standard;

import java.util.Properties;
import java.util.StringTokenizer;

import com.ramussoft.common.AbstractPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;

public class AutochangePlugin extends AbstractPlugin {

    public static final String AUTO_ADD_ATTRIBUTES = "/user/qualifier/auto-add-attributes.xml";

    public static final String AUTO_ADD_ATTRIBUTE_IDS = "AUTO_ADD_ATTRIBUTE_IDS";

    public static final String ATTRIBUTE_FOR_NAME = "ATTRIBUTE_FOR_NAME";

    @Override
    public String getName() {
        return "Autochange";
    }

    @Override
    public void init(final Engine engine, AccessRules accessor) {
        super.init(engine, accessor);
        engine.addQualifierListener(new QualifierAdapter() {
            @Override
            public void qualifierCreated(QualifierEvent event) {
                if (event.isJournaled())
                    return;

                if (StandardAttributesPlugin.isDisableAutoupdate(engine))
                    return;

                Qualifier qualifier = event.getNewQualifier();
                if (!qualifier.isSystem()) {
                    Properties ps = engine.getProperties(AUTO_ADD_ATTRIBUTES);
                    String ids = ps.getProperty(AUTO_ADD_ATTRIBUTE_IDS);
                    String attributeForName = ps
                            .getProperty(ATTRIBUTE_FOR_NAME);
                    if (ids == null)
                        ids = "";
                    StringTokenizer st = new StringTokenizer(ids, " ,");
                    boolean added = false;
                    while (st.hasMoreElements()) {
                        String s = st.nextToken();
                        long id = Long.parseLong(s);
                        Attribute attr = engine.getAttribute(id);
                        if (attr != null) {
                            added = true;
                            qualifier.getAttributes().add(attr);
                            if (s.equals(attributeForName))
                                qualifier.setAttributeForName(attr.getId());
                        }
                    }
                    if (added) {
                        engine.updateQualifier(qualifier);
                    }
                }
            }
        });
    }

}

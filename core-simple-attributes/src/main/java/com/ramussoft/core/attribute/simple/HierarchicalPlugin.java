package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;

public class HierarchicalPlugin extends SimpleAttributePlugin {

    public static final String HIERARHICAL_ATTRIBUTE = "HierarchicalAttribute";

    private Attribute hierarhicalAttribute = null;

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{HierarchicalPersistent.class};
    }

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Object toObject(Persistent persistent) {
                return persistent;
            }

            @Override
            protected Persistent toPersistent(Object value) {
                return (Persistent) value;
            }

        };
    }

    @Override
    public String getTypeName() {
        return "Hierarchical";
    }

    @Override
    public void init(final Engine engine, AccessRules accessor) {
        super.init(engine, accessor);
        hierarhicalAttribute = engine.getSystemAttribute(HIERARHICAL_ATTRIBUTE);

        if (hierarhicalAttribute == null) {
            hierarhicalAttribute = engine
                    .createSystemAttribute(new AttributeType(getName(),
                            getTypeName(), true));
            hierarhicalAttribute.setName(HIERARHICAL_ATTRIBUTE);
            engine.updateAttribute(hierarhicalAttribute);
        }

        engine.setPluginProperty(getName(), HIERARHICAL_ATTRIBUTE,
                hierarhicalAttribute);

        engine.addQualifierListener(new QualifierAdapter() {
            @Override
            public void qualifierCreated(QualifierEvent event) {
                if (event.isJournaled())
                    return;
                if (StandardAttributesPlugin.isDisableAutoupdate(engine))
                    return;
                Qualifier qualifier = event.getNewQualifier();
                if (qualifier.isSystem())
                    return;
                qualifier.getSystemAttributes().add(hierarhicalAttribute);
                engine.updateQualifier(qualifier);
            }
        });
    }

    @Override
    public boolean isSystem() {
        return true;
    }
}

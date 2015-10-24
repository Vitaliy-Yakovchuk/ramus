package com.ramussoft.idef0.attribute;

import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class SectorPropertiesPlugin extends AbstractAttributePlugin {

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
        return "SectorProperties";
    }

    @Override
    public boolean isComparable() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{SectorPropertiesPersistent.class};
    }

}

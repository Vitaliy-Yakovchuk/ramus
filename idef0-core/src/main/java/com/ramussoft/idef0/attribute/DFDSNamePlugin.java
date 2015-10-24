package com.ramussoft.idef0.attribute;

import com.ramussoft.common.AttributeType;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class DFDSNamePlugin extends AbstractAttributePlugin {

    public static final String ROLE = "\\role";

    public static final String TERM = "\\term";

    public static AttributeType type = new AttributeType("IDEF0", "DFDSName");

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Persistent toPersistent(Object value) {
                return (Persistent) value;
            }

            @Override
            protected Object toObject(Persistent persistent) {
                return persistent;
            }
        };
    }

    @Override
    public String getTypeName() {
        return "DFDSName";
    }

    @Override
    public boolean isComparable() {
        return true;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{DFDSName.class};
    }
}

package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class DoublePlugin extends SimpleAttributePlugin {

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{DoublePersistent.class};
    }

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Object toObject(Persistent persistent) {
                return ((DoublePersistent) persistent).getValue();
            }

            @Override
            protected Persistent toPersistent(Object value) {
                return new DoublePersistent((Double) value);
            }

        };
    }

    @Override
    public String getTypeName() {
        return "Double";
    }

}

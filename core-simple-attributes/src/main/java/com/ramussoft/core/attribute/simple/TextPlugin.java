package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.attribute.FindObject;
import com.ramussoft.common.persistent.Persistent;

public class TextPlugin extends SimpleAttributePlugin {

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{TextPersistent.class};
    }

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Object toObject(Persistent persistent) {
                return ((TextPersistent) persistent).getValue();
            }

            @Override
            protected Persistent toPersistent(Object value) {
                return new TextPersistent((String) value);
            }

            @Override
            public FindObject[] getFindObjects(Object object) {
                if (object == null)
                    return null;
                return new FindObject[]{new FindObject("value", object)};
            }
        };
    }

    @Override
    public String getTypeName() {
        return "Text";
    }

}

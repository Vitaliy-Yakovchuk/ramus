package com.ramussoft.core.attribute.simple;

import java.sql.Timestamp;
import java.util.Date;

import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class DatePlugin extends SimpleAttributePlugin {

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{DatePersistent.class};
    }

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Object toObject(Persistent persistent) {
                return ((DatePersistent) persistent).getValue();
            }

            @Override
            protected Persistent toPersistent(Object value) {
                if (value instanceof Timestamp) {
                    return new DatePersistent((Timestamp) value);
                }
                return new DatePersistent(new Timestamp(((Date) value)
                        .getTime()));
            }

        };
    }

    @Override
    public String getTypeName() {
        return "Date";
    }
}

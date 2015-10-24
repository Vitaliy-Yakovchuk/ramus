package com.ramussoft.core.attribute.simple;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class CurrencyPlugin extends SimpleAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    if (object == null)
                        return new List[]{new ArrayList(0)};
                    else {
                        List list = new ArrayList(1);
                        list.add(object);
                        return new List[]{list};
                    }
                }
                return super.toPersistens(object, elementId, attributeId,
                        engine);
            }

            public Object toObject(java.util.List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    List<Persistent> list = persistents[0];
                    if (list.size() == 0)
                        return null;
                    else
                        return list.get(0);
                }
                return super.toObject(persistents, elementId, attributeId,
                        engine);
            }

            ;

            @Override
            protected Persistent toPersistent(Object value) {
                CurrencyPersistent cp = new CurrencyPersistent();
                cp.setValue(((Number) value).doubleValue());
                return cp;
            }

            @Override
            protected Object toObject(Persistent persistent) {
                return ((CurrencyPersistent) persistent).getValue();
            }
        };
    }

    @Override
    public String getTypeName() {
        return "Currency";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{CurrencyPersistent.class};
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePropertyPersistents() {
        return new Class[]{CurrencyPropertyPersistent.class};
    }
}

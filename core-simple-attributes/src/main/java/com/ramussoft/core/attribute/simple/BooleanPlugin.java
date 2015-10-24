package com.ramussoft.core.attribute.simple;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class BooleanPlugin extends SimpleAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                if (object == null)
                    return new List[]{new ArrayList<Persistent>(0)};
                ArrayList<Persistent> l = new ArrayList<Persistent>(1);
                l.add(new BooleanPersistent(((Boolean) object) ? 1 : 0));
                return new List[]{l};
            }

            @SuppressWarnings("unchecked")
            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                List l = persistents[0];
                if (l.size() == 0)
                    return null;
                BooleanPersistent p = (BooleanPersistent) l.get(0);
                return p.getValue() != 0;
            }
        };
    }

    @Override
    public String getTypeName() {
        return "Boolean";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{BooleanPersistent.class};
    }
}

package com.ramussoft.core.attribute.simple;

import java.util.ArrayList;

import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class IconPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {
            public Object toObject(
                    java.util.List<com.ramussoft.common.persistent.Persistent>[] persistents,
                    long elementId, long attributeId,
                    com.ramussoft.common.IEngine engine) {
                if (persistents[0].size() == 0)
                    return null;
                return persistents[0].get(0);
            }

            ;

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                List<Persistent>[] res = new List[1];
                res[0] = new ArrayList<Persistent>(1);
                if (object != null)
                    res[0].add((Persistent) object);
                return res;
            }
        };
    }

    @Override
    public String getTypeName() {
        return "Icon";
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
        return "Core";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{IconPersistent.class};
    }

}

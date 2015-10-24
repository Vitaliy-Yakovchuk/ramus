package com.ramussoft.eval;

import java.util.ArrayList;

import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class FunctionPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @SuppressWarnings("unchecked")
            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                return new ArrayList(persistents[0]);
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                if (object == null) {
                    return new List[]{new ArrayList<Persistent>(0)};
                }
                return new List[]{new ArrayList((List) object)};
            }

        };
    }

    @Override
    public String getTypeName() {
        return "Function";
    }

    @Override
    public boolean isComparable() {
        return true;
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public String getName() {
        return "Eval";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{FunctionPersistent.class};
    }

    @Override
    public boolean isLight() {
        return false;
    }
}

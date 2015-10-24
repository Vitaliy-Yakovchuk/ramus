package com.ramussoft.core.attribute.simple;

import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class TablePlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    return persistents[0];
                }
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    if (object != null)
                        return new List[]{(List) object};
                }
                return new List[]{};
            }

        };
    }

    @Override
    public String getTypeName() {
        return "Table";
    }

    @Override
    public boolean isComparable() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public boolean isLight() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePropertyPersistents() {
        return new Class[]{TableGroupablePropertyPersistent.class};
    }
}

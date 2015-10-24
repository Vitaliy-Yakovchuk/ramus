package com.ramussoft.idef0.attribute;

import java.util.Collections;
import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class SectorPointPlugin extends AbstractAttributePlugin {

    @Override
    public boolean isLight() {
        return true;
    }

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                List list = (List) persistents[0];
                Collections.sort(list);
                return persistents[0];
            }

            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                List<SectorPointPersistent> list = (List) object;
                for (int i = 0; i < list.size(); i++)
                    list.get(i).setPosition(i);
                return new List[]{list};
            }
        };
    }

    @Override
    public String getTypeName() {
        return "SectorPoint";
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
        return new Class[]{SectorPointPersistent.class};
    }
}

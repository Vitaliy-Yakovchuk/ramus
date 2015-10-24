package com.ramussoft.core.attribute.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class PricePlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                if (object == null)
                    object = new Price(new ArrayList(0));
                List list = new ArrayList(Arrays.asList(((Price) object).getData()));
                for (int i = 0; i < list.size(); i++) {
                    ((PricePersistent) list.get(i)).setPosition(i);
                }
                return new List[]{list};
            }

            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                if (persistents.length == 0)
                    return null;
                Collections.sort((List) persistents[0]);
                return new Price(new ArrayList(persistents[0]));
            }
        };
    }

    @Override
    public String getTypeName() {
        return "Price";
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
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{PricePersistent.class};
    }

}

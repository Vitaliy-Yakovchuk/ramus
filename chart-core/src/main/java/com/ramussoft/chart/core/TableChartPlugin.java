package com.ramussoft.chart.core;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class TableChartPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                List<Persistent> res = new ArrayList<Persistent>(1);
                if (object != null) {
                    Persistent p = (Persistent) object;
                    if (p != null)
                        res.add(p);
                }
                return new List[]{res};
            }

            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                if (persistents.length == 0)

                    return null;
                List<Persistent> list = persistents[0];
                if (list.size() == 1)
                    return list.get(0);
                return null;
            }
        };
    }

    @Override
    public String getTypeName() {
        return "TableChart";
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
        return "Chart";
    }

    @Override
    public Class<? extends Persistent>[] getAttributePropertyPersistents() {
        return new Class[]{TableChartPersistent.class};
    }

}

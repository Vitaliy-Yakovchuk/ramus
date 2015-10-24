package com.ramussoft.chart.core;

import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class ChartLinkPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Persistent toPersistent(Object value) {
                ChartLinkPersistent p = new ChartLinkPersistent();
                p.setOtherElementId((Long) value);
                return p;
            }

            @Override
            protected Object toObject(Persistent persistent) {
                return ((ChartLinkPersistent) persistent).getOtherElementId();
            }
        };
    }

    @Override
    public String getTypeName() {
        return "Link";
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

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{ChartLinkPersistent.class};
    }

    @Override
    public boolean isLight() {
        return false;
    }
}

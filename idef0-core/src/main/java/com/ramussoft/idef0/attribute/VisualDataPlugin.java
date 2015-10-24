package com.ramussoft.idef0.attribute;

import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class VisualDataPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Object toObject(Persistent persistent) {
                return ((VisualDataPersisitent) persistent).getData();
            }

            @Override
            protected Persistent toPersistent(Object value) {
                VisualDataPersisitent p = new VisualDataPersisitent();
                p.setData((byte[]) value);
                return p;
            }

        };
    }

    @Override
    public String getTypeName() {
        return "VisualData";
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
        return new Class[]{VisualDataPersisitent.class};
    }

}

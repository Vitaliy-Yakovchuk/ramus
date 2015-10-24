package com.ramussoft.idef0.attribute;

import java.awt.Color;

import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class ColorPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {
            @Override
            protected Object toObject(Persistent persistent) {
                return new Color(((ColorPersistent) persistent).getColor());
            }

            @Override
            protected Persistent toPersistent(Object value) {
                ColorPersistent p = new ColorPersistent();
                p.setColor(((Color) value).getRGB());
                return p;
            }
        };
    }

    @Override
    public String getTypeName() {
        return "Color";
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
        return new Class[]{ColorPersistent.class};
    }
}

package com.ramussoft.idef0.attribute;

import java.awt.Font;

import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class FontPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Object toObject(Persistent persistent) {
                FontPersistent p = (FontPersistent) persistent;
                return new Font(p.getName(), p.getStyle(), p.getSize());
            }

            @Override
            protected Persistent toPersistent(Object value) {
                Font f = (Font) value;
                FontPersistent p = new FontPersistent();
                p.setName(f.getName());
                p.setStyle(f.getStyle());
                p.setSize(f.getSize());
                return p;
            }

        };
    }

    @Override
    public String getTypeName() {
        return "Font";
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
        return new Class[]{FontPersistent.class};
    }

}

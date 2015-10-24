package com.ramussoft.idef0.attribute;

import com.dsoft.pb.types.FRectangle;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class FRectanglePlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Object toObject(Persistent persistent) {
                FRectanglePersistent fp = (FRectanglePersistent) persistent;
                return new FRectangle(fp.getX(), fp.getY(), fp.getWidth(), fp
                        .getHeight());
            }

            @Override
            protected Persistent toPersistent(Object value) {
                FRectanglePersistent e = new FRectanglePersistent();
                FRectangle rect = (FRectangle) value;
                e.setX(rect.getX());
                e.setY(rect.getY());
                e.setWidth(rect.getWidth());
                e.setHeight(rect.getHeight());
                return e;
            }

        };
    }

    @Override
    public String getTypeName() {
        return "FRectangle";
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
        return new Class[]{FRectanglePersistent.class};
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[]{"Core"};
    }

}

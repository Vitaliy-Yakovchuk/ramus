package com.ramussoft.core.attribute.simple;

import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public abstract class AbstractFilePlugin extends AbstractAttributePlugin {
    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                return AbstractFilePlugin.this.toObject(persistents, elementId,
                        attributeId, engine);
            }

            @Override
            @SuppressWarnings("unchecked")
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                AbstractFilePlugin.this.toPersistens((byte[]) object,
                        elementId, attributeId, engine);
                return new List[]{};
            }

        };
    }

    protected byte[] toObject(
            java.util.List<com.ramussoft.common.persistent.Persistent>[] persistents,
            long elementId, long attributeId,
            com.ramussoft.common.IEngine engine) {
        return engine.getStream(getFilePath(elementId, attributeId));
    }

    public String getFilePath(long elementId, long attributeId) {
        return "/elements/" + elementId + "/" + attributeId
                + "/Core/" + getFileName();
    }

    ;

    protected abstract String getFileName();

    protected void toPersistens(byte[] object, long elementId,
                                long attributeId, IEngine engine) {
        String path = getFilePath(elementId, attributeId);
        if (object == null) {
            engine.deleteStream(path);
        } else
            engine.setStream(path, object);
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

}
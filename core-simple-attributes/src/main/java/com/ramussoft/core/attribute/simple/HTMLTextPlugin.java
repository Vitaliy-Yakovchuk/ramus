package com.ramussoft.core.attribute.simple;

import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class HTMLTextPlugin extends AbstractFilePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                byte[] bs = HTMLTextPlugin.this.toObject(persistents,
                        elementId, attributeId, engine);
                if (bs == null)
                    return null;
                return new HTMLPage(bs, getFilePath(elementId, attributeId));
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                if (object == null)
                    HTMLTextPlugin.this.toPersistens(null, elementId,
                            attributeId, engine);
                else
                    HTMLTextPlugin.this.toPersistens(
                            ((HTMLPage) object).getData(), elementId,
                            attributeId, engine);
                return new List[]{};
            }

        };
    }

    @Override
    public String getTypeName() {
        return "HTMLText";
    }

    @Override
    protected String getFileName() {
        return "index.html";
    }

    @Override
    public boolean isLight() {
        return false;
    }

    @Override
    public boolean isHistorySupport() {
        return true;
    }
}

package com.ramussoft.core.attribute.simple;

import java.util.ArrayList;

import java.util.List;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class FilePlugin extends AbstractFilePlugin {

    public static final String PLUGIN_NAME = "Plugin.Core.File";

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{FilePersistent.class};
    }

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public Object toObject(final List<Persistent>[] persistents,
                                   final long elementId, final long attributeId,
                                   final IEngine engine) {
                if (persistents[0].size() == 0)
                    return null;
                FilePersistent p = (FilePersistent) persistents[0].get(0);
                p.setData(FilePlugin.this.toObject(persistents, elementId,
                        attributeId, engine));
                return p;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                List<Persistent>[] res = new List[]{new ArrayList<Persistent>(
                        1)};
                if (object != null) {
                    FilePersistent p = (FilePersistent) object;
                    res[0].add(p);
                    FilePlugin.this.toPersistens(p.getData(), elementId,
                            attributeId, engine);
                } else {
                    FilePlugin.this.toPersistens(null, elementId,
                            attributeId, engine);
                }
                return res;
            }

        };
    }

    @Override
    public String getTypeName() {
        return "File";
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public boolean isComparable() {
        return false;
    }

    @Override
    public boolean isLight() {
        return false;
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    protected String getFileName() {
        return "file";
    }

    @Override
    public void init(Engine engine, AccessRules accessor) {
        super.init(engine, accessor);
        engine.setPluginProperty(getName(), PLUGIN_NAME, this);
    }

    @Override
    public boolean isHistorySupport() {
        return true;
    }
}

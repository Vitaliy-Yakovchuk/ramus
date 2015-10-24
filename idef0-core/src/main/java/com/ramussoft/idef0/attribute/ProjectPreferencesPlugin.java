package com.ramussoft.idef0.attribute;

import java.util.ArrayList;
import java.util.List;

import com.dsoft.pb.idef.elements.ProjectOptions;
import com.dsoft.pb.idef.elements.Readed;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public class ProjectPreferencesPlugin extends AbstractAttributePlugin {

    @Override
    public String getName() {
        return "IDEF0";
    }

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                ProjectOptions result;
                if (persistents[0].size() == 0)
                    return null;
                else
                    result = new ProjectOptions(
                            (IDEF0ModelPreferencesPersistent) persistents[0]
                                    .get(0));
                for (Persistent p : persistents[1])
                    result.getReadedModel().addReaded().setDeligate(
                            (ReaderPersistent) p);
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                ProjectOptions po = (ProjectOptions) object;
                List<Persistent>[] res = new List[2];
                List<Persistent> f = new ArrayList<Persistent>();
                f.add(po.getDeligate());
                res[0] = f;
                List<Persistent> s = new ArrayList<Persistent>();
                for (Readed r : po.getReadedModel().getAllReaded()) {
                    s.add(r.getDeligate());
                }
                res[1] = s;
                return res;
            }

        };
    }

    @Override
    public String getTypeName() {
        return "ProjectPreferences";
    }

    @Override
    public boolean isComparable() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{IDEF0ModelPreferencesPersistent.class,
                ReaderPersistent.class};
    }

}

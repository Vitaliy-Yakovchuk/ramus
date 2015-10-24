package com.ramussoft.core.attribute.simple;

import java.util.ArrayList;
import java.util.Collections;

import java.util.List;

import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Transaction;

public class VariantPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {
            @SuppressWarnings("unchecked")
            public Object toObject(java.util.List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    List list = persistents[0];
                    if (list != null)
                        Collections.sort(list);
                    return persistents[0];
                } else {
                    List<Persistent> list = persistents[0];
                    if (list.size() != 1)
                        return null;

                    List<Persistent> ps = engine.getBinaryAttribute(-1,
                            attributeId)[0];
                    VariantPersistent vp = (VariantPersistent) list.get(0);
                    for (Persistent p : ps) {
                        VariantPropertyPersistent pp = (VariantPropertyPersistent) p;
                        if (pp.getVariantId() == vp.getVariantId())
                            return pp.getValue();
                    }

                    return "WARNING: Unknown variant with variant id: "
                            + vp.getVariantId();
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    List<VariantPropertyPersistent> list = (List) object;
                    for (int i = 0; i < list.size(); i++)
                        list.get(i).setPosition(i);
                    return new List[]{list};
                } else {
                    if (object == null) {
                        return new List[]{new ArrayList(0)};
                    }

                    List<Persistent> ps = engine.getBinaryAttribute(-1,
                            attributeId)[0];

                    VariantPersistent vp = new VariantPersistent();
                    long maxId = -1;
                    for (Persistent p : ps) {
                        VariantPropertyPersistent pp = (VariantPropertyPersistent) p;
                        if (pp.getValue().equals(object)) {
                            vp.setVariantId(pp.getVariantId());
                            List<Persistent> list = new ArrayList<Persistent>(1);
                            list.add(vp);
                            return new List[]{list};
                        }
                        if (maxId < pp.getVariantId())
                            maxId = pp.getVariantId();
                    }

                    VariantPropertyPersistent pp = new VariantPropertyPersistent();
                    pp.setValue((String) object);
                    pp.setVariantId(maxId + 1l);
                    pp.setAttribute(attributeId);
                    pp.setPosition(ps.size());
                    vp.setVariantId(pp.getVariantId());

                    if (engine instanceof Engine) {
                        ps.add(pp);
                        ((Engine) engine).setAttribute(null, engine
                                .getAttribute(attributeId), ps);
                    } else {
                        Transaction transaction = new Transaction();
                        transaction.getSave().add(pp);
                        engine.setBinaryAttribute(-1, attributeId, transaction);
                    }

                    List<Persistent> list = new ArrayList<Persistent>(1);
                    list.add(vp);
                    return new List[]{list};
                }
            }

        };
    }

    @Override
    public String getTypeName() {
        return "Variant";
    }

    @Override
    public boolean isComparable() {
        return true;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public String getName() {
        return "Core";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{VariantPersistent.class};
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePropertyPersistents() {
        return new Class[]{VariantPropertyPersistent.class};
    }
}

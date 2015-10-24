package com.ramussoft.core.attribute.simple;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.persistent.Persistent;

public abstract class SimpleAttributeConverter extends
        AbstractAttributeConverter {

    public Object toObject(List<Persistent>[] persistents, long elementId,
                           long attributeId, IEngine engine) {
        List<Persistent> list = persistents[0];
        if (list.size() == 0) {
            return null;
        }
        if (list.size() == 1) {
            return toObject(list.get(0));
        }
        throw new RuntimeException(MessageFormat.format(
                "Size of list for simple persistent object must be 1 or 0 {0}",
                list));
    }

    ;

    @SuppressWarnings("unchecked")
    @Override
    public List<Persistent>[] toPersistens(Object object, long elementId,
                                           long attributeId, IEngine engine) {
        List<Persistent> res = new ArrayList<Persistent>(1);
        if (object != null) {
            Persistent p = toPersistent(object);
            if (p != null)
                res.add(p);
        }
        return new List[]{res};
    }

    protected abstract Persistent toPersistent(Object value);

    protected abstract Object toObject(Persistent persistent);
}

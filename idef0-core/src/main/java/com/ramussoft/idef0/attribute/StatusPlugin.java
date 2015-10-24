package com.ramussoft.idef0.attribute;

import com.dsoft.pb.idef.elements.Status;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class StatusPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Object toObject(Persistent persistent) {
                StatusPersistent p = (StatusPersistent) persistent;
                return new Status(p.getType(), p.getOtherName());
            }

            @Override
            protected Persistent toPersistent(Object value) {
                StatusPersistent p = new StatusPersistent();
                Status s = (Status) value;
                p.setOtherName(s.getAtherName());
                p.setType(s.getType());
                return p;
            }

        };
    }

    @Override
    public String getTypeName() {
        return "Status";
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
        return new Class[]{StatusPersistent.class};
    }

}

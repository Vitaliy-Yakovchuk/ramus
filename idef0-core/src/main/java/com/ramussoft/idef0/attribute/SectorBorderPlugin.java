package com.ramussoft.idef0.attribute;

import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class SectorBorderPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {
            /*public Object toObject(List<Persistent>[] persistents,
                    long elementId, long attributeId, IEngine engine) {
				List<Persistent> list = persistents[0];
				if (list.size() == 0) {
					return new SectorBorderPersistent();
				}
				return toObject(list.get(0));

			};*/

            @Override
            protected Object toObject(Persistent persistent) {
                return persistent;
            }

            @Override
            protected Persistent toPersistent(Object value) {
                return (Persistent) value;
            }
        };
    }

    @Override
    public String getTypeName() {
        return "SectorBorder";
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
        return new Class[]{SectorBorderPersistent.class};
    }
}

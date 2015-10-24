package com.ramussoft.idef0.attribute;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.attribute.EngineParalleler;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.SimpleAttributeConverter;

public class FunctionOwnerPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new SimpleAttributeConverter() {

            @Override
            protected Object toObject(Persistent persistent) {
                return ((FunctionOunerPersistent) persistent).getOunerId();
            }

            @Override
            protected Persistent toPersistent(Object value) {
                FunctionOunerPersistent p = new FunctionOunerPersistent();
                p.setOunerId((Long) value);
                return p;
            }

        };
    }

    @Override
    public String getTypeName() {
        return "OunerId";
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
        return new Class[]{FunctionOunerPersistent.class};
    }

    @Override
    public void copyAttribute(Engine sourceEngine, Engine destinationEngine,
                              Attribute sourceAttribute, Attribute destinationAttribute,
                              Element sourceElement, Element destinationElement,
                              EngineParalleler paralleler) {
        if (sourceElement != null) {
            Long id = (Long) sourceEngine.getAttribute(sourceElement,
                    sourceAttribute);
            if (id != null) {
                destinationEngine.setAttribute(destinationElement,
                        destinationAttribute, getId(paralleler.getElement(id)));
            }
        }
    }

    private Object getId(Element element) {
        if (element == null)
            return null;
        return element.getId();
    }
}

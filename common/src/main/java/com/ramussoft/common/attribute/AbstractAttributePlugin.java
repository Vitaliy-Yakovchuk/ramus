package com.ramussoft.common.attribute;

import java.util.ArrayList;
import java.util.Arrays;

import com.ramussoft.common.AbstractPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.PersistentField;
import com.ramussoft.common.persistent.PersistentRow;

public abstract class AbstractAttributePlugin extends AbstractPlugin implements
        AttributePlugin {

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{};
    }

    @Override
    public void fillAttributeQuery(PersistentRow row, long attributeId,
                                   long elementId, ArrayList<Object> params,
                                   ArrayList<String> paramFields, IEngine engine) {
        for (PersistentField field : row.getFields()) {
            if (field.isAutoset()) {
                if ((field.getType() == PersistentField.ELEMENT)
                        && (elementId >= 0)) {
                    params.add(elementId);
                    paramFields.add(field.getDatabaseName());
                } else if ((attributeId >= 0)
                        && (field.getType() == PersistentField.ATTRIBUTE)) {
                    params.add(attributeId);
                    paramFields.add(field.getDatabaseName());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePropertyPersistents() {
        return new Class[]{};
    }

    @Override
    public String toString() {
        return getName() + "." + getTypeName();
    }

    @Override
    public boolean isLight() {
        return true;
    }

    @Override
    public Class<? extends Persistent>[] getPersistents() {
        Class<? extends Persistent>[] classes1 = getAttributePersistents();
        Class<? extends Persistent>[] classes2 = getAttributePropertyPersistents();
        Class<? extends Persistent>[] classes = Arrays.copyOf(classes1,
                classes1.length + classes2.length);
        for (int i = classes1.length; i < classes.length; i++)
            classes[i] = classes2[i - classes1.length];
        return classes;
    }

    @Override
    public void copyAttribute(Engine sourceEngine, Engine destinationEngine,
                              Attribute sourceAttribute, Attribute destinationAttribute,
                              Element sourceElement, Element destinationElement,
                              EngineParalleler paralleler) {
        if ((sourceElement != null) && (destinationElement != null)) {
            Object object = sourceEngine.getAttribute(sourceElement,
                    sourceAttribute);
            if (object != null) {
                destinationEngine.setAttribute(destinationElement,
                        destinationAttribute, object);
            }
        }
    }

    @Override
    public void init(Engine engine, AccessRules accessor) {
        super.init(engine, accessor);
        engine.setPluginProperty(getName(), getTypeName() + ".Plugin", this);
    }

    @Override
    public Object toUserValue(Engine engine, Attribute attribute,
                              Element element, Object value) {
        return value;
    }

    @Override
    public boolean isHistorySupport() {
        return isLight();
    }
}

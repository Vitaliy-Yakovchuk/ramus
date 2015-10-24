package com.ramussoft.common.attribute;

import java.util.ArrayList;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.PersistentRow;

/**
 * Interface which add attribute type, with its storage, etc.
 *
 * @author zdd
 */

public interface AttributePlugin extends Plugin {

    /**
     * Return classes which describes attribute storage structure.
     */

    public Class<? extends Persistent>[] getPersistents();

    /**
     * Return engine class for attributes.
     */

    public AttributeConverter getAttributeConverter();

    /**
     * Return name of the type.
     */

    public String getTypeName();

    /**
     * Is type object comparable.
     */

    public boolean isComparable();

    /**
     * Is attribute type system.
     */

    public boolean isSystem();

    public Class<? extends Persistent>[] getAttributePersistents();

    /**
     * Fill attribute query for element.
     *
     * @param row
     * @param attributeId
     * @param elementId
     * @param qualifierId
     * @param params
     * @param paramFields
     */

    public void fillAttributeQuery(PersistentRow row, long attributeId,
                                   long elementId, ArrayList<Object> params,
                                   ArrayList<String> paramFields, IEngine engine);

    public Class<? extends Persistent>[] getAttributePropertyPersistents();

    /**
     * Return <code>true</code> if attribute is light and can be loaded with
     * tables, etc.
     *
     * @return <code>true</code> is object is light, <code>false</code> if not.
     */

    public boolean isLight();

    /**
     * Copy attribute data from one element of one engine to another.
     *
     * @param sourceEngine         Source engine.
     * @param destinationEngine    Destination engine.
     * @param sourceAttribute      Source attribute
     * @param destinationAttribute Destination attribute
     * @param sourceElement        Source element, can be <code>null</code> if attribute
     *                             preferences will be copied.
     * @param destinationElement   Destination element, can be <code>null</code> if attribute
     *                             preferences will be copied.
     * @param paralleler           Interface which let to make some manipulation with data.
     */

    public void copyAttribute(Engine sourceEngine, Engine destinationEngine,
                              Attribute sourceAttribute, Attribute destinationAttribute,
                              Element sourceElement, Element destinationElement,
                              EngineParalleler paralleler);

    public Object toUserValue(Engine engine, Attribute attribute,
                              Element element, Object value);

    public boolean isHistorySupport();

}

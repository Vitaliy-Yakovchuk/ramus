package com.ramussoft.common.attribute;

import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.persistent.Persistent;

public interface AttributeConverter {

    /**
     * Method converts object from persistent objects to equivalent value
     * object.
     *
     * @param persistents Array of persistent objects.
     * @return New converted object.
     */

    public Object toObject(List<Persistent>[] persistents, long elementId,
                           long attributeId, IEngine engine);

    /**
     * Method converts object to persistent objects.
     */

    public List<Persistent>[] toPersistens(Object object, long elementId,
                                           long attributeId, IEngine engine);

    public FindObject[] getFindObjects(Object object);

}

package com.ramussoft.common.attribute;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;

public interface EngineParalleler {

    /**
     * Return equals attribute in destination engine.
     *
     * @param sourceAttributeId Can be -1l, if source attribute with such an id not exists,
     *                          method will throw runtime exception.
     * @return Return attribute with equals id or <code>null</code> if
     * sourceAttributeId is -1l.
     */

    Attribute getAttribute(long sourceAttributeId);

    /**
     * Return equals qualifier in destination engine.
     *
     * @param sourceQualifierId Can be -1l, if source qualifier with such an id not exists,
     *                          method will throw runtime exception.
     * @return Return qualifier with equals id or <code>null</code> if
     * sourceQualifierId is -1l.
     */

    Qualifier getQualifier(long sourceQualifierId);

    /**
     * Return equals element in destination engine.
     *
     * @param sourceElementId Can be -1l, if source element with such an id not exists,
     *                        method will throw runtime exception.
     * @return Return element with equals id or <code>null</code> if
     * sourceElementId is -1l.
     */

    Element getElement(long sourceElementId);

}

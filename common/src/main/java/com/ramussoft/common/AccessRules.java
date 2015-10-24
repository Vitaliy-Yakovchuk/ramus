package com.ramussoft.common;

public interface AccessRules {

    boolean canReadQualifier(long qualifierId);

    boolean canReadElement(long elementId);

    /**
     * @param elementId   Can be -1
     * @param attributeId
     * @return
     */

    boolean canReadElement(long elementId, long attributeId);

    boolean canReadAttribute(long qualifierId, long attributeId);

    boolean canUpdateQualifier(long qualifierId);

    boolean canUpdateElement(long elementId, long attributeId);

    boolean canUpdateAttribute(long attribueId);

    boolean canUpdateAttribute(long qualifierId, long attributeId);

    boolean canDeleteQualifier(long qualifierId);

    boolean canDeleteElements(long[] elementIds);

    boolean canDeleteAttribute(long attributeId);

    boolean canCreateQualifier();

    boolean canCreateElement(long qualifierId);

    boolean canCreateAttribute();

    DeleteStatusList getElementsDeleteStatusList(long[] elementIds);

    boolean canUpdateStream(String path);

    boolean canCreateStript();
}

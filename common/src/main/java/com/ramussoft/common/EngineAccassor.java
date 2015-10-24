package com.ramussoft.common;

public interface EngineAccassor extends AccessRules {

    DeleteStatusList getAttributeDeleteStatus(long qualifierId,
                                              long attributeId);

    DeleteStatusList getAttributeDeleteStatus(long attributeId);

    DeleteStatusList getQualifierDeleteStatus(long qualifierId);

}

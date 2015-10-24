package com.ramussoft.core.impl;

import java.util.Vector;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.IntegrityAccessor;

public abstract class AbstractIEngineAccessor extends IntegrityAccessor
        implements AccessRules {

    public AbstractIEngineAccessor(IEngine engine) {
        super(engine);
    }

    @Override
    public boolean canDeleteElements(long[] elementIds) {
        Vector<Long> qualifierIds = new Vector<Long>();
        for (long elementId : elementIds) {
            Long qualifierId = engine.getQualifierIdForElement(elementId);
            if (qualifierIds.indexOf(qualifierId) < 0) {
                if (!canUpdateQualifier(qualifierId))
                    return false;
                else
                    qualifierIds.add(qualifierId);
            }
        }
        return true;
    }

    @Override
    public boolean canReadElement(long elementId) {
        return canReadQualifier(engine.getQualifierIdForElement(elementId));
    }

    @Override
    public boolean canReadElement(long elementId, long attributeId) {
        return canReadAttribute(engine.getQualifierIdForElement(elementId),
                attributeId);
    }

    @Override
    public boolean canUpdateElement(long elementId, long attributeId) {
        return canUpdateAttribute(engine.getQualifierIdForElement(elementId),
                attributeId);
    }

}

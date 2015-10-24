package com.ramussoft.core.impl;

import java.util.Arrays;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.DeleteStatusList;

public class IntegrityAccessorSuit implements AccessRules {

    private AccessRules[] accessors = new AccessRules[]{};

    @Override
    public boolean canCreateAttribute() {
        for (AccessRules accessor : accessors) {
            if (!accessor.canCreateAttribute()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canCreateElement(long quaifierId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canCreateElement(quaifierId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canCreateQualifier() {
        for (AccessRules accessor : accessors) {
            if (!accessor.canCreateQualifier()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canDeleteAttribute(long attributeId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canDeleteAttribute(attributeId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canDeleteElements(long[] elementIds) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canDeleteElements(elementIds)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canDeleteQualifier(long qualifierId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canDeleteQualifier(qualifierId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canReadAttribute(long qualifierId, long attributeId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canReadAttribute(qualifierId, attributeId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canReadElement(long elementId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canReadElement(elementId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canReadElement(long elementId, long attributeId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canReadElement(elementId, attributeId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canReadQualifier(long qualifierId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canReadQualifier(qualifierId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUpdateAttribute(long attribueId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canUpdateAttribute(attribueId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUpdateAttribute(long qualifierId, long attributeId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canUpdateAttribute(qualifierId, attributeId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUpdateElement(long elementId, long attributeId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canUpdateElement(elementId, attributeId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUpdateQualifier(long qualifierId) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canUpdateQualifier(qualifierId)) {
                return false;
            }
        }
        return true;
    }

    public void addAccessRules(AccessRules accessor) {
        accessors = Arrays.copyOf(accessors, accessors.length + 1);
        accessors[accessors.length - 1] = accessor;
    }

    public void clearAccessors() {
        this.accessors = new AccessRules[]{};
    }

    @Override
    public DeleteStatusList getElementsDeleteStatusList(long[] elementIds) {
        DeleteStatusList list = new DeleteStatusList();
        for (AccessRules rules : accessors) {
            list.addAll(rules.getElementsDeleteStatusList(elementIds));
        }
        return list;
    }

    @Override
    public boolean canUpdateStream(String path) {
        for (AccessRules accessor : accessors) {
            if (!accessor.canUpdateStream(path)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canCreateStript() {
        for (AccessRules accessor : accessors) {
            if (!accessor.canCreateStript()) {
                return false;
            }
        }
        return true;
    }
}

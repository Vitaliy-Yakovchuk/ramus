package com.ramussoft.common;

import java.util.List;

public abstract class IntegrityAccessor implements AccessRules {

    protected IEngine engine;

    public IntegrityAccessor(IEngine engine) {
        this.engine = engine;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canDeleteAttribute(long attributeId) {
        List<Qualifier> qualifiers = engine.getQualifiers();
        qualifiers.addAll(engine.getSystemQualifiers());
        for (Qualifier qualifier : qualifiers)
            if (!qualifier.getName().equals("HistoryQualifier")) {
                for (Attribute a : qualifier.getAttributes()) {
                    if (!isElementList(a.getAttributeType()))
                        if (a.getId() == attributeId)
                            return false;
                }
                for (Attribute a : qualifier.getSystemAttributes()) {
                    if (a.getId() == attributeId)
                        return false;
                }
            }
        return true;
    }

    private boolean isElementList(AttributeType attributeType) {
        return (("Core".equals(attributeType.getPluginName()) && ("ElementList"
                .equals(attributeType.getTypeName()))));
    }

    @Override
    public boolean canDeleteQualifier(long qualifierId) {
        return engine.getElements(qualifierId).size() == 0;
    }

}

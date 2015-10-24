package com.ramussoft.common;

public abstract class AbstractPlugin implements Plugin {

    protected Engine engine;

    protected AccessRules rules;

    @Override
    public DeleteStatus getElementDeleteStatus(long qualifierId,
                                               long attributeId, IEngine engine) {
        return null;
    }

    @Override
    public DeleteStatus getAttributeDeleteStatus(long attributeId,
                                                 IEngine engine) {
        return null;
    }

    @Override
    public boolean canDeleteElements(long[] elementIds, IEngine engine) {
        return true;
    }

    @Override
    public DeleteStatus getQualifierDeleteStatus(long qualifierId,
                                                 IEngine engine) {
        return null;
    }

    @Override
    public void init(Engine engine, AccessRules rules) {
        this.engine = engine;
        this.rules = rules;
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[]{};
    }

    @Override
    public String[] getSequences() {
        return new String[]{};
    }

    @Override
    public DeleteStatus getElementsDeleteStatus(long[] elementIds,
                                                IEngine engine) {
        return null;
    }

    @Override
    public void replaceElements(Engine engine, Element[] oldElements,
                                Element newElement) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class getFunctionalInterface() {
        return null;
    }

    @Override
    public Object createFunctionalInterfaceObject(Engine engine, IEngine iEngine) {
        return null;
    }

    @Override
    public boolean isCriticatToOpenFile() {
        return true;
    }

    @Override
    public String[] getGroups() {
        return new String[]{};
    }
}

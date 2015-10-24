package com.ramussoft.common;

public interface Plugin {

    String getName();

    DeleteStatus getElementDeleteStatus(long qualifierId, long attributeId,
                                        IEngine engine);

    DeleteStatus getAttributeDeleteStatus(long attributeId, IEngine engine);

    boolean canDeleteElements(long[] elementIds, IEngine engine);

    DeleteStatus getQualifierDeleteStatus(long qualifierId, IEngine engine);

    void init(Engine engine, AccessRules accessor);

    String[] getRequiredPlugins();

    String[] getSequences();

    DeleteStatus getElementsDeleteStatus(long[] elementIds, IEngine engine);

    void replaceElements(Engine engine, Element[] oldElements,
                         Element newElement);

    @SuppressWarnings("unchecked")
    Class getFunctionalInterface();

    Object createFunctionalInterfaceObject(Engine engine, IEngine iEngine);

    boolean isCriticatToOpenFile();

    String[] getGroups();

}

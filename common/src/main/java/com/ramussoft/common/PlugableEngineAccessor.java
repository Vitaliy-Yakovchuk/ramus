package com.ramussoft.common;

public class PlugableEngineAccessor implements EngineAccassor {

    protected AccessRules deligate;

    protected PluginFactory pluginFactory;

    private IEngine engine;

    public PlugableEngineAccessor(IEngine engine, AccessRules deligate,
                                  PluginFactory pluginFactory) {
        this.deligate = deligate;
        this.pluginFactory = pluginFactory;
        this.engine = engine;
    }

    @Override
    public DeleteStatusList getAttributeDeleteStatus(long qualifierId,
                                                     long attributeId) {
        DeleteStatusList statusList = new DeleteStatusList();
        for (Plugin plugin : pluginFactory.getPlugins()) {
            statusList.add(plugin.getElementDeleteStatus(qualifierId,
                    attributeId, engine));
        }
        return statusList;
    }

    @Override
    public DeleteStatusList getAttributeDeleteStatus(long attributeId) {
        DeleteStatusList statusList = new DeleteStatusList();
        for (Plugin plugin : pluginFactory.getPlugins()) {
            statusList
                    .add(plugin.getAttributeDeleteStatus(attributeId, engine));
        }
        return statusList;
    }

    @Override
    public DeleteStatusList getElementsDeleteStatusList(long[] elementIds) {
        DeleteStatusList statusList = new DeleteStatusList();
        for (Plugin plugin : pluginFactory.getPlugins()) {
            statusList.add(plugin.getElementsDeleteStatus(elementIds, engine));
        }
        return statusList;
    }

    @Override
    public boolean canCreateAttribute() {
        return deligate.canCreateAttribute();
    }

    @Override
    public boolean canCreateElement(long quaifierId) {
        return deligate.canCreateElement(quaifierId);
    }

    @Override
    public boolean canCreateQualifier() {
        return deligate.canCreateQualifier();
    }

    @Override
    public boolean canDeleteAttribute(long attributeId) {
        if (deligate.canDeleteAttribute(attributeId)) {
            return getAttributeDeleteStatus(attributeId).canDelete();
        }
        return false;
    }

    @Override
    public boolean canDeleteElements(long[] elementIds) {
        if (deligate.canDeleteElements(elementIds)) {
            for (Plugin plugin : pluginFactory.getPlugins()) {
                if (!plugin.canDeleteElements(elementIds, engine))
                    return false;
            }
        } else
            return false;
        return true;
    }

    @Override
    public boolean canDeleteQualifier(long qualifierId) {
        if (deligate.canDeleteQualifier(qualifierId)) {
            return getQualifierDeleteStatus(qualifierId).canDelete();
        }
        return false;
    }

    @Override
    public boolean canReadAttribute(long qualifierId, long attributeId) {
        return deligate.canReadAttribute(qualifierId, attributeId);
    }

    @Override
    public boolean canReadElement(long elementId) {
        return deligate.canReadElement(elementId);
    }

    @Override
    public boolean canReadElement(long elementId, long attributeId) {
        return deligate.canReadElement(elementId, attributeId);
    }

    @Override
    public boolean canReadQualifier(long qualifierId) {
        return deligate.canReadQualifier(qualifierId);
    }

    @Override
    public boolean canUpdateAttribute(long attribueId) {
        return deligate.canUpdateAttribute(attribueId);
    }

    @Override
    public boolean canUpdateAttribute(long qualifierId, long attributeId) {
        return deligate.canUpdateAttribute(qualifierId, attributeId);
    }

    @Override
    public boolean canUpdateElement(long elementId, long attributeId) {
        return deligate.canUpdateElement(elementId, attributeId);
    }

    @Override
    public boolean canUpdateQualifier(long qualifierId) {
        return deligate.canUpdateQualifier(qualifierId);
    }

    @Override
    public DeleteStatusList getQualifierDeleteStatus(long qualifierId) {
        DeleteStatusList statusList = new DeleteStatusList();
        for (Plugin plugin : pluginFactory.getPlugins()) {
            statusList
                    .add(plugin.getQualifierDeleteStatus(qualifierId, engine));
        }
        return statusList;
    }

    @Override
    public boolean canUpdateStream(String path) {
        return deligate.canUpdateStream(path);
    }

    @Override
    public boolean canCreateStript() {
        return deligate.canCreateStript();
    }
}

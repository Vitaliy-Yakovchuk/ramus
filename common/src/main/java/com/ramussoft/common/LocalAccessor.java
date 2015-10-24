package com.ramussoft.common;

public abstract class LocalAccessor extends IntegrityAccessor {

    public LocalAccessor(IEngine engine) {
        super(engine);
    }

    public abstract boolean isBranchLeaf();

    @Override
    public boolean canCreateAttribute() {
        return true;// isBranchLeaf();
    }

    @Override
    public boolean canCreateElement(long quaifierId) {
        return true;// isBranchLeaf();
    }

    @Override
    public boolean canCreateQualifier() {
        return true;// isBranchLeaf();
    }

    @Override
    public boolean canDeleteElements(long[] elementIds) {
        return true;// isBranchLeaf();
    }

    @Override
    public boolean canReadAttribute(long qualifierId, long attributeId) {
        return true;
    }

    @Override
    public boolean canReadElement(long elementId) {
        return true;
    }

    @Override
    public boolean canReadElement(long elementId, long attributeId) {
        return true;
    }

    @Override
    public boolean canReadQualifier(long qualifierId) {
        return true;
    }

    @Override
    public boolean canUpdateAttribute(long attribueId) {
        return true;// isBranchLeaf();
    }

    @Override
    public boolean canUpdateAttribute(long qualifierId, long attributeId) {
        return true;// isBranchLeaf();
    }

    @Override
    public boolean canUpdateElement(long elementId, long attributeId) {
        return true;// isBranchLeaf();
    }

    @Override
    public boolean canUpdateQualifier(long qualifierId) {
        return true;// isBranchLeaf();
    }

    @Override
    public DeleteStatusList getElementsDeleteStatusList(long[] elementIds) {
        if (isBranchLeaf()) {
            return new DeleteStatusList();
        }
        return new DeleteStatusList() {

            /**
             *
             */
            private static final long serialVersionUID = -5992508902203844497L;

            public boolean canDelete() {
                return false;
            }

            ;
        };
    }

    @Override
    public boolean canUpdateStream(String path) {
        return true;
    }

    @Override
    public boolean canCreateStript() {
        return isBranchLeaf();
    }

}

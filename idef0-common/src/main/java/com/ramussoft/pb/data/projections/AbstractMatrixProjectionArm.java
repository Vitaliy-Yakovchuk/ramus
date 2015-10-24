package com.ramussoft.pb.data.projections;

import com.ramussoft.pb.Row;
import com.ramussoft.pb.data.AbstractDataPlugin;

public abstract class AbstractMatrixProjectionArm extends
        AbstractMatrixProjection {

    protected Row row1 = null;

    protected Row row2 = null;

    protected AbstractDataPlugin dataPlugin;

    public AbstractMatrixProjectionArm(final AbstractDataPlugin dataPlugin) {
        super();
        this.dataPlugin = dataPlugin;
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
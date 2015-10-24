package com.ramussoft.pb.data.negine;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.pb.data.AbstractRow;

public class NRow extends AbstractRow {

    public NRow(final NDataPlugin dataPlugin, Element element, RowSet rowSet,
                Attribute[] attributes, Object[] objects) {
        super(dataPlugin, element, rowSet, attributes, objects);
    }

    @Override
    public boolean remove() {
        if (!isRemoveable() || dataPlugin.isStatic(globalId))
            return false;
        super.remove();
        return true;
    }

    public boolean isCanSetName() {
        return false;
    }

    @Override
    public Qualifier getQualifier() {
        return engine.getQualifier(engine
                .getQualifierIdForElement(getElementId()));
    }

    public long getQualifierId() {
        return getElement().getQualifierId();
    }
}

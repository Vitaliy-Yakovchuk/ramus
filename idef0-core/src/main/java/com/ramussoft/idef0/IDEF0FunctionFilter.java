package com.ramussoft.idef0;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

import com.ramussoft.database.common.ElementLoadFilter;
import com.ramussoft.database.common.RowSet;

public class IDEF0FunctionFilter implements ElementLoadFilter {

    @Override
    public boolean load(Element element, RowSet rowSet) {
        Engine engine = rowSet.getEngine();
        Attribute attribute = IDEF0Plugin.getFunctionTypeAttribute(engine);

        if (rowSet.getQualifier().getSystemAttributes().contains(attribute)) {
            Integer type = (Integer) engine.getAttribute(element, attribute);
            if (type != null)
                return type < 1001;
        }

        return true;
    }

}

package com.ramussoft.database.common;

import com.ramussoft.common.Element;

public interface ElementLoadFilter {

    boolean load(Element element, RowSet rowSet);

}

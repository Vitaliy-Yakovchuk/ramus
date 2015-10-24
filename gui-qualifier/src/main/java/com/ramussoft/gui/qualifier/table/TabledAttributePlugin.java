package com.ramussoft.gui.qualifier.table;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public interface TabledAttributePlugin {

    ValueGetter getValueGetter(Attribute attribute, Engine engine, GUIFramework framework, Closeable model);

}

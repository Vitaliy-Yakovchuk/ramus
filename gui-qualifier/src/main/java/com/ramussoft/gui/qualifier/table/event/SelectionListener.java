package com.ramussoft.gui.qualifier.table.event;

import java.util.EventListener;

public interface SelectionListener extends EventListener {

    void changeSelection(SelectionEvent event);

}

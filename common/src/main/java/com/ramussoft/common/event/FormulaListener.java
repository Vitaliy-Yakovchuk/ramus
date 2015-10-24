package com.ramussoft.common.event;

import java.util.EventListener;

public interface FormulaListener extends EventListener {

    void formulaChanged(FormulaEvent event);

}

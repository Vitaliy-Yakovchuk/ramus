package com.ramussoft.common.event;

import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Engine;

public class FormulaEvent extends Event {

    /**
     *
     */
    private static final long serialVersionUID = -3871910191813557587L;

    private CalculateInfo oldFormula;

    private CalculateInfo newFormula;

    public FormulaEvent(Engine engine, boolean journaled,
                        CalculateInfo oldFormula, CalculateInfo newFormula) {
        super(engine, journaled);
        this.oldFormula = oldFormula;
        this.newFormula = newFormula;
    }

    public CalculateInfo getOldFormula() {
        return oldFormula;
    }

    public CalculateInfo getNewFormula() {
        return newFormula;
    }

}

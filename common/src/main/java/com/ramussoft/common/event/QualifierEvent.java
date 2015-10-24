package com.ramussoft.common.event;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;

public class QualifierEvent extends Event {

    /**
     *
     */
    private static final long serialVersionUID = -5382192964668045524L;

    private Qualifier oldQualifier;

    private Qualifier newQualifier;

    public QualifierEvent(Engine engine, Qualifier oldQualifier,
                          Qualifier newQualifier) {
        this(engine, oldQualifier, newQualifier, false);
    }

    public QualifierEvent(Engine engine, Qualifier oldQualifier,
                          Qualifier newQualifier, boolean journaled) {
        super(engine, journaled);
        this.oldQualifier = oldQualifier;
        this.newQualifier = newQualifier;
    }

    public Qualifier getOldQualifier() {
        return oldQualifier;
    }

    public Qualifier getNewQualifier() {
        return newQualifier;
    }

    public Qualifier getQualifier() {
        if (newQualifier != null)
            return newQualifier;
        return oldQualifier;
    }

}

package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class CreateQualifierCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = 5586797305216551616L;

    private long qualifierId;

    private boolean system;

    public CreateQualifierCommand(JournaledEngine engine) {
        super(engine);
    }

    public CreateQualifierCommand(JournaledEngine engine, Qualifier qualifier) {
        super(engine);
        this.qualifierId = qualifier.getId();
        this.system = qualifier.isSystem();
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        qualifierId = input.readLong();
        system = input.readBoolean();
    }

    @Override
    public void redo(IEngine iEngine) {
        Qualifier qualifier;
        if (system)
            qualifier = iEngine.createSystemQualifier(qualifierId);
        else
            qualifier = iEngine.createQualifier(qualifierId);
        QualifierEvent event = new QualifierEvent(engine, null, qualifier, true);
        engine.qualifierCreated(event);
    }

    @Override
    public void undo(IEngine iEngine) {
        Qualifier old = iEngine.getQualifier(qualifierId);
        iEngine.deleteQualifier(qualifierId);
        QualifierEvent event = new QualifierEvent(engine, old, null, true);
        engine.qualifierDeleted(event);
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        output.writeLong(qualifierId);
        output.writeBoolean(system);
    }

}

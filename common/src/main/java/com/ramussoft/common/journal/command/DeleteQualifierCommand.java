package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class DeleteQualifierCommand extends QualifierStorageCommand {

    /**
     *
     */
    private static final long serialVersionUID = 7573950849202048687L;

    private Qualifier qualifier;

    private boolean system;

    public DeleteQualifierCommand(JournaledEngine engine) {
        super(engine);
    }

    public DeleteQualifierCommand(JournaledEngine engine, Qualifier qualifier) {
        super(engine);
        this.qualifier = qualifier;
        system = qualifier.isSystem();
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        qualifier = new Qualifier();
        loadQualifier(input, qualifier);
        system = input.readBoolean();
    }

    @Override
    public void redo(IEngine iEngine) {
        iEngine.deleteQualifier(qualifier.getId());
        QualifierEvent event = new QualifierEvent(engine, qualifier, null, true);
        engine.qualifierDeleted(event);
    }

    @Override
    public void undo(IEngine iEngine) {
        Qualifier old;
        if (system)
            old = iEngine.createSystemQualifier(qualifier.getId());
        else
            old = iEngine.createQualifier(qualifier.getId());
        QualifierEvent event = new QualifierEvent(engine, null, old, true);
        engine.qualifierCreated(event);
        iEngine.updateQualifier(qualifier);
        event = new QualifierEvent(engine, old, qualifier, true);
        engine.qualifierUpdated(event);
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        storeQualifier(output, qualifier);
        output.writeBoolean(system);
    }

}

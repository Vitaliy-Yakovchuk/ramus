package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.Element;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class CreateElementCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = -5335594989752142660L;

    private long qualifierId;

    private long elementId;

    public CreateElementCommand(JournaledEngine engine) {
        super(engine);
    }

    public CreateElementCommand(JournaledEngine engine, long qualifierId,
                                long elementId) {
        super(engine);
        this.qualifierId = qualifierId;
        this.elementId = elementId;
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        qualifierId = input.readLong();
        elementId = input.readLong();

    }

    @Override
    public void redo(IEngine iEngine) {
        Element element = iEngine.createElement(qualifierId, elementId);
        ElementEvent event = new ElementEvent(engine, null, element,
                qualifierId, true);
        engine.elementCreated(event);
    }

    @Override
    public void undo(IEngine iEngine) {
        Element element = iEngine.getElement(elementId);
        ElementEvent event = new ElementEvent(engine, element, null,
                qualifierId, true);
        engine.beforeElementDeleted(event);

        iEngine.deleteElement(elementId);
        engine.elementDeleted(event);
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        output.writeLong(qualifierId);
        output.writeLong(elementId);
    }

}

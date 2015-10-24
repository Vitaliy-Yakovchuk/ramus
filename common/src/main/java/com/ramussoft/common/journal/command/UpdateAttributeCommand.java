package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class UpdateAttributeCommand extends AttributeStorageCommand {

    /**
     *
     */
    private static final long serialVersionUID = 3561255393080643912L;

    private Attribute oldAttribute;

    private Attribute newAttribute;

    public UpdateAttributeCommand(JournaledEngine engine) {
        super(engine);
    }

    public UpdateAttributeCommand(JournaledEngine engine,
                                  Attribute oldAttribute, Attribute newQualifier) {
        super(engine);
        this.oldAttribute = oldAttribute;
        this.newAttribute = newQualifier;
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        oldAttribute = new Attribute();
        newAttribute = new Attribute();
        loadAttribute(input, oldAttribute);
        loadAttribute(input, newAttribute);
    }

    @Override
    public void redo(IEngine iEngine) {
        iEngine.updateAttribute(newAttribute);
        AttributeEvent event = new AttributeEvent(engine, null, newAttribute,
                oldAttribute, newAttribute, true);
        for (AttributeListener l : engine.getAttributeListeners()) {
            l.attributeUpdated(event);
        }
    }

    @Override
    public void undo(IEngine iEngine) {
        iEngine.updateAttribute(oldAttribute);
        AttributeEvent event = new AttributeEvent(engine, null, oldAttribute,
                newAttribute, oldAttribute, true);
        for (AttributeListener l : engine.getAttributeListeners()) {
            l.attributeUpdated(event);
        }
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        storeAttribute(output, oldAttribute);
        storeAttribute(output, newAttribute);
    }

}

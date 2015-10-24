package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class CreateAttributeCommand extends AttributeStorageCommand {

    /**
     *
     */
    private static final long serialVersionUID = -628640654499719792L;
    private Attribute attribute;

    public CreateAttributeCommand(JournaledEngine engine) {
        super(engine);
    }

    public CreateAttributeCommand(JournaledEngine engine, Attribute attribute) {
        super(engine);
        this.attribute = attribute;
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        attribute = new Attribute();
        loadAttribute(input, attribute);
    }

    @Override
    public void redo(IEngine iEngine) {
        Attribute attr = iEngine.createAttribute(attribute.getId(), attribute
                .getAttributeType());
        AttributeEvent event = new AttributeEvent(engine, null, attr, null,
                attr, true);
        engine.attributeCreated(event);
    }

    @Override
    public void undo(IEngine iEngine) {
        iEngine.deleteAttribute(attribute.getId());
        AttributeEvent event = new AttributeEvent(engine, null, attribute, attribute,
                null, true);
        engine.attributeDeleted(event);
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        storeAttribute(output, attribute);
    }

}

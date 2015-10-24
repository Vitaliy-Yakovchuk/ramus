package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Transaction;

public class DeleteAttributeCommand extends AttributeStorageCommand {

    /**
     *
     */
    private static final long serialVersionUID = -2146134939516947087L;

    private Attribute attribute;

    private Transaction properties;

    public DeleteAttributeCommand(JournaledEngine engine) {
        super(engine);
    }

    public DeleteAttributeCommand(JournaledEngine engine, Attribute attribute) {
        this(engine);
        this.attribute = attribute;
        properties = engine.getAttributePropertyWhatWillBeDeleted(attribute
                .getId());
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        attribute = new Attribute();
        properties = new Transaction();
        int c = input.readInt();
        for (int i = 0; i < c; i++)
            try {
                properties.getSave()
                        .add(
                                TransactionStorageCommand.loadPersistent(
                                        engine, input));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        loadAttribute(input, attribute);
    }

    @Override
    public void redo(IEngine iEngine) {
        AttributeEvent event = new AttributeEvent(engine, null, attribute,
                attribute, null, true);
        engine.beforeAttributeDeleted(event);
        iEngine.deleteAttribute(attribute.getId());
        engine.attributeDeleted(event);
    }

    @Override
    public void undo(IEngine iEngine) {
        Attribute old = iEngine.createAttribute(attribute.getId(), attribute
                .getAttributeType());

        iEngine.setBinaryAttribute(-1, attribute.getId(), properties);

        AttributeEvent event = new AttributeEvent(engine, null, attribute,
                null, attribute, true);
        engine.attributeCreated(event);
        iEngine.updateAttribute(attribute);
        event = new AttributeEvent(engine, null, attribute, old, attribute,
                true);
        engine.attributeChanged(event);
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        output.writeInt(properties.getDelete().size());
        for (Persistent persistent : properties.getDelete()) {
            TransactionStorageCommand.storePersistent(engine, output,
                    persistent);
        }
        storeAttribute(output, attribute);
    }

}

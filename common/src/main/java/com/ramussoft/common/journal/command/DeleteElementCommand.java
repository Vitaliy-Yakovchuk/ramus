package com.ramussoft.common.journal.command;

import java.io.IOException;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Transaction;

public class DeleteElementCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = -3479284793292827153L;

    private long qualifierId;

    private long elementId;

    private Transaction[] data;

    public DeleteElementCommand(JournaledEngine engine) {
        super(engine);
    }

    public DeleteElementCommand(JournaledEngine engine, long qualifierId,
                                long elementId, Transaction[] data) {
        super(engine);
        this.qualifierId = qualifierId;
        this.elementId = elementId;
        this.data = data;
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        qualifierId = input.readLong();
        elementId = input.readLong();
        int size = input.readInt();
        data = new Transaction[size];
        for (int i = 0; i < size; i++) {
            Transaction transaction = new Transaction();
            transaction.setRemoveBranchInfo(input.readBoolean());
            data[i] = transaction;
            int size2 = input.readInt();
            for (int j = 0; j < size2; j++) {
                try {
                    transaction.getSave().add(
                            TransactionStorageCommand.loadPersistent(engine,
                                    input));
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void redo(IEngine iEngine) {
        Element old = iEngine.getElement(elementId);
        ElementEvent event = new ElementEvent(engine, old, null, qualifierId,
                true);
        engine.beforeElementDeleted(event);
        iEngine.deleteElement(elementId);
        engine.elementDeleted(event);
    }

    @Override
    public void undo(IEngine iEngine) {
        Qualifier qualifier = iEngine.getQualifier(qualifierId);

        Element element = iEngine.createElement(qualifierId, elementId);

        ElementEvent event = new ElementEvent(engine, null, element,
                qualifierId, true);
        engine.elementCreated(event);


        List<Attribute> attrs = qualifier.getAttributes();
        attrs.addAll(qualifier.getSystemAttributes());
        int i = 0;
        for (Attribute attr : attrs) {
            iEngine.setBinaryAttribute(elementId, attr.getId(), data[i]);
            Object object = engine.getAttribute(element, attr);
            if ((attr.getId() == qualifier.getAttributeForName())
                    && (object != null)) {
                element.setName(object.toString());
            }
            AttributeEvent event2 = new AttributeEvent(engine, element, attr,
                    null, object, true);
            engine.attributeChanged(event2);
            i++;
        }

    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        output.writeLong(qualifierId);
        output.writeLong(elementId);
        output.writeInt(data.length);
        for (Transaction transaction : data) {
            output.writeBoolean(transaction.isRemoveBranchInfo());
            output.writeInt(transaction.getDelete().size());
            for (Persistent persistent : transaction.getDelete()) {
                TransactionStorageCommand.storePersistent(engine, output,
                        persistent);
            }
        }
    }

}

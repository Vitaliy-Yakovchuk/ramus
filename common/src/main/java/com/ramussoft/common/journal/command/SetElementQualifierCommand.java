package com.ramussoft.common.journal.command;

import java.io.IOException;
import java.util.ArrayList;
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

public class SetElementQualifierCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = -2657050529735270344L;

    private long oldQualifierId;

    private long newQualifierId;

    private long elementId;

    private Transaction[] data;

    public SetElementQualifierCommand(JournaledEngine engine) {
        super(engine);
    }

    public SetElementQualifierCommand(JournaledEngine engine,
                                      long oldQualifierId, long newQualifierId, long elementId,
                                      Transaction[] data) {
        super(engine);
        this.oldQualifierId = oldQualifierId;
        this.newQualifierId = newQualifierId;
        this.elementId = elementId;
        this.data = data;
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        oldQualifierId = input.readLong();
        newQualifierId = input.readLong();
        elementId = input.readLong();
        int size = input.readInt();
        data = new Transaction[size];
        for (int i = 0; i < size; i++) {
            Transaction transaction = new Transaction();
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
        Element newElement = new Element(old.getId(), newQualifierId, old
                .getName());
        ElementEvent event = new ElementEvent(engine, old, newElement,
                oldQualifierId, true);
        engine.beforeElementDeleted(event);
        iEngine.setElementQualifier(elementId, newQualifierId);
        engine.removeElementQualifierFromBuffer(elementId);
        engine.elementDeleted(event);
        engine.elementCreated(event);
    }

    @Override
    public void undo(IEngine iEngine) {
        Element element = iEngine.getElement(elementId);

        Element newElement = new Element(element.getId(), oldQualifierId,
                element.getName());

        ElementEvent event = new ElementEvent(engine, element, newElement,
                newQualifierId, true);
        engine.beforeElementDeleted(event);
        iEngine.setElementQualifier(elementId, oldQualifierId);
        engine.elementDeleted(event);
        event = new ElementEvent(engine, element, newElement, oldQualifierId, true);
        engine.elementCreated(event);
        Qualifier oldQualifier = iEngine.getQualifier(oldQualifierId);

        Qualifier newQualifier = iEngine.getQualifier(newQualifierId);

        List<Attribute> attrs = new ArrayList<Attribute>();
        addNotPresentAttributes(oldQualifier.getAttributes(), newQualifier
                .getAttributes(), attrs);
        addNotPresentAttributes(oldQualifier.getSystemAttributes(),
                newQualifier.getSystemAttributes(), attrs);
        List<Attribute> as = oldQualifier.getAttributes();
        as.addAll(oldQualifier.getSystemAttributes());

        int i = 0;
        for (Attribute a : as) {
            if (attrs.indexOf(a) >= 0) {
                iEngine.setBinaryAttribute(elementId, a.getId(), data[i]);
                i++;
            }
            Object object = engine.getAttribute(newElement, a);
            if (object != null) {
                AttributeEvent event2 = new AttributeEvent(engine, newElement,
                        a, null, object, true);
                engine.attributeChanged(event2);
            }
        }
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        output.writeLong(oldQualifierId);
        output.writeLong(newQualifierId);
        output.writeLong(elementId);
        output.writeInt(data.length);
        for (Transaction transaction : data) {
            output.writeInt(transaction.getDelete().size());
            for (Persistent persistent : transaction.getDelete()) {
                TransactionStorageCommand.storePersistent(engine, output,
                        persistent);
            }
        }
    }

    private void addNotPresentAttributes(List<Attribute> attributes,
                                         List<Attribute> attributes2, List<Attribute> attrs) {
        for (Attribute attribute : attributes)
            if (attributes2.indexOf(attribute) < 0)
                attrs.add(attribute);
    }

}

package com.ramussoft.common.journal.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Transaction;

public class UpdateQualifierCommand extends QualifierStorageCommand {

    /**
     *
     */
    private static final long serialVersionUID = 2461822487251443461L;

    private Qualifier oldQualifier;

    private Qualifier newQualifier;

    private List<Attribute> deletedAttributes;

    private Hashtable<Long, Hashtable<Long, Transaction>> hashtable = new Hashtable<Long, Hashtable<Long, Transaction>>();

    public UpdateQualifierCommand(JournaledEngine engine) {
        super(engine);
    }

    public UpdateQualifierCommand(JournaledEngine engine,
                                  Qualifier oldQualifier, Qualifier newQualifier) {
        super(engine);
        this.oldQualifier = oldQualifier;
        this.newQualifier = newQualifier;
        initDeletedAttributes();
        for (Attribute attribute : deletedAttributes) {
            Hashtable<Element, Transaction> t = engine
                    .getAttributeWhatWillBeDeleted(oldQualifier.getId(),
                            attribute.getId());
            Hashtable<Long, Transaction> put = new Hashtable<Long, Transaction>();
            hashtable.put(attribute.getId(), put);
            for (Entry<Element, Transaction> entry : t.entrySet()) {
                put.put(entry.getKey().getId(), entry.getValue());
            }
        }
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        oldQualifier = new Qualifier();
        newQualifier = new Qualifier();
        loadQualifier(input, oldQualifier);
        loadQualifier(input, newQualifier);
        initDeletedAttributes();

        for (Attribute attribute : deletedAttributes) {
            Hashtable<Long, Transaction> hash = new Hashtable<Long, Transaction>();
            hashtable.put(attribute.getId(), hash);
            int size1 = input.readInt();
            for (int i = 0; i < size1; i++) {
                Transaction transaction = new Transaction();
                Long key = input.readLong();
                int size2 = input.readInt();
                for (int j = 0; j < size2; j++) {
                    try {
                        transaction.getSave().add(
                                TransactionStorageCommand.loadPersistent(
                                        engine, input));
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                hash.put(key, transaction);
            }
        }

    }

    private void initDeletedAttributes() {
        deletedAttributes = new ArrayList<Attribute>();
        addToDeletedAttributes(oldQualifier.getAttributes(), newQualifier
                .getAttributes());
        addToDeletedAttributes(oldQualifier.getSystemAttributes(), newQualifier
                .getSystemAttributes());
    }

    private void addToDeletedAttributes(List<Attribute> oldAttributes,
                                        List<Attribute> newAttributes) {
        for (Attribute old : oldAttributes) {
            if (newAttributes.indexOf(old) < 0)
                deletedAttributes.add(old);
        }
    }

    @Override
    public void redo(IEngine iEngine) {
        iEngine.updateQualifier(newQualifier);
        QualifierEvent event = new QualifierEvent(engine, oldQualifier,
                newQualifier, true);
        if (oldQualifier.getAttributeForName() != newQualifier
                .getAttributeForName())
            getEngine().updateElementNames(oldQualifier, newQualifier);
        engine.qualifierUpdated(event);
    }

    @Override
    public void undo(IEngine iEngine) {
        iEngine.updateQualifier(oldQualifier);

        QualifierEvent event = new QualifierEvent(engine, newQualifier,
                oldQualifier, true);

        for (Attribute attribute : deletedAttributes) {
            for (Entry<Long, Transaction> entry : hashtable.get(
                    attribute.getId()).entrySet()) {
                iEngine.setBinaryAttribute(entry.getKey(), attribute.getId(),
                        entry.getValue());
                Element element = iEngine.getElement(entry.getKey());
                AttributeEvent event2 = new AttributeEvent(engine, element,
                        attribute, null, engine
                        .getAttribute(element, attribute), true);
                engine.attributeChanged(event2);
            }
        }

        if (oldQualifier.getAttributeForName() != newQualifier
                .getAttributeForName())
            getEngine().updateElementNames(newQualifier, oldQualifier);
        engine.qualifierUpdated(event);
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        storeQualifier(output, oldQualifier);
        storeQualifier(output, newQualifier);
        for (Attribute attribute : deletedAttributes) {
            Hashtable<Long, Transaction> hash = hashtable
                    .get(attribute.getId());
            output.writeInt(hash.size());
            for (Entry<Long, Transaction> entry : hash.entrySet()) {
                output.writeLong(entry.getKey());
                output.writeInt(entry.getValue().getDelete().size());
                for (Persistent p : entry.getValue().getDelete()) {
                    TransactionStorageCommand
                            .storePersistent(engine, output, p);
                }
            }
        }
    }

}

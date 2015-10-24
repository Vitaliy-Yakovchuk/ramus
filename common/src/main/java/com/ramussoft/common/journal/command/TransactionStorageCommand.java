package com.ramussoft.common.journal.command;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.PersistentField;
import com.ramussoft.common.persistent.PersistentRow;
import com.ramussoft.common.persistent.PersistentWrapper;
import com.ramussoft.common.persistent.Transaction;

import static com.ramussoft.common.persistent.PersistentField.*;

public class TransactionStorageCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = -1140574642314391613L;

    private List<Persistent> save = new ArrayList<Persistent>();

    private List<Persistent> delete = new ArrayList<Persistent>();

    private List<Persistent> updateOld = new ArrayList<Persistent>();

    private List<Persistent> updateNew = new ArrayList<Persistent>();

    private long attributeId;

    private long elementId;

    private boolean removeBranchInfo = false;

    public TransactionStorageCommand(JournaledEngine engine) {
        super(engine);
    }

    public TransactionStorageCommand(JournaledEngine engine, long elementId,
                                     long attributeId, Transaction transaction) {
        this(engine);
        this.attributeId = attributeId;
        this.elementId = elementId;
        this.removeBranchInfo = transaction.isRemoveBranchInfo();
        copy(transaction.getSave(), save);
        copy(transaction.getUpdate(), updateNew);
        copy(transaction.getOldUpdate(), updateOld);
        copy(transaction.getDelete(), delete);
    }

    private void copy(List<Persistent> source, List<Persistent> destination) {
        for (Persistent p : source)
            destination.add(p);
    }

    protected void storePersistent(BinaryDataOutput output,
                                   Persistent persistent) throws IOException {
        storePersistent(engine, output, persistent);
    }

    static void storePersistent(JournaledEngine engine,
                                BinaryDataOutput output, Persistent persistent) throws IOException {
        Class<? extends Persistent> key = persistent.getClass();
        byte id = (byte) engine.getPersistentClassId(key);
        output.writeByte(id);
        PersistentRow row = engine.getPersistentMetadata(key);
        List<PersistentField> fields = new ArrayList<PersistentField>();
        for (PersistentField field : row.getFields())
            if (!field.isAutoset())
                fields.add(field);

        output.write(fields.size());
        for (PersistentField field : fields) {
            output.writeByte(field.getFieldId());
            String fieldName = field.getName();
            PersistentWrapper wrapper = engine.getWrapper(key);
            Object object = wrapper.getField(persistent, fieldName);

            if (object == null)
                output.write(0);
            else {
                output.write(1);

                switch (field.getType()) {
                    case ATTRIBUTE:
                    case ELEMENT:
                    case QUALIFIER:
                    case ID:
                    case LONG:
                        output.writeLong((Long) object);
                        break;
                    case TEXT:
                        output.writeSwimedString((String) object);
                        break;
                    case DATE:
                        output.writeLong(((Timestamp) object).getTime());
                        break;
                    case DOUBLE:
                        output.writeDouble((Double) object);
                        break;
                    case PersistentField.BINARY:
                        output.writeBinary((byte[]) object);
                        break;
                    case PersistentField.INTEGER:
                        output.writeInt((Integer) object);
                        break;
                    default:
                        throw new RuntimeException("Unknown field type: "
                                + field.getType());
                }
            }
        }
        output.writeLong(persistent.getValueBranchId());
    }

    protected Persistent loadPersistent(BinaryDataInput input)
            throws IOException, InstantiationException, IllegalAccessException {
        return loadPersistent(engine, input);
    }

    static Persistent loadPersistent(JournaledEngine engine,
                                     BinaryDataInput input) throws IOException, InstantiationException,
            IllegalAccessException {
        Class<? extends Persistent> key = engine.getPersistentClassById(input
                .read());
        PersistentRow row = engine.getPersistentMetadata(key);
        Persistent persistent = key.newInstance();
        int size = input.read();
        for (int i = 0; i < size; i++) {
            int fieldId = input.read();
            PersistentField field = row.getFieldById(fieldId);
            String fieldName = field.getName();
            PersistentWrapper wrapper = engine.getWrapper(key);
            if (input.read() == 1)
                switch (field.getType()) {
                    case ATTRIBUTE:
                    case ELEMENT:
                    case QUALIFIER:
                    case ID:
                    case LONG:
                        wrapper.setField(persistent, fieldName, input.readLong());
                        break;
                    case TEXT:
                        wrapper.setField(persistent, fieldName,
                                input.readSwimedString());
                        break;
                    case DATE:
                        wrapper.setField(persistent, fieldName,
                                new Timestamp(input.readLong()));
                        break;
                    case DOUBLE:
                        wrapper.setField(persistent, fieldName, input.readDouble());
                        break;
                    case BINARY:
                        wrapper.setField(persistent, fieldName, input.readBinary());
                        break;
                    case INTEGER:
                        wrapper.setField(persistent, fieldName, input.readInt());
                        break;
                    default:
                        throw new RuntimeException("Unknown field type: "
                                + field.getType());
                }
        }
        persistent.setValueBranchId(input.readLong());
        return persistent;
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        try {
            removeBranchInfo = input.readBoolean();
            attributeId = input.readLong();
            elementId = input.readLong();
            read(save, input);
            read(delete, input);
            read(updateOld, input);
            read(updateNew, input);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void read(List<Persistent> list, BinaryDataInput input)
            throws IOException, InstantiationException, IllegalAccessException {
        while (input.readByte() == 1) {
            list.add(loadPersistent(input));
        }
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        output.writeBoolean(removeBranchInfo);
        output.writeLong(attributeId);
        output.writeLong(elementId);
        write(save, output);
        write(delete, output);
        write(updateOld, output);
        write(updateNew, output);
    }

    private void write(List<Persistent> list, BinaryDataOutput output)
            throws IOException {
        for (Persistent p : list) {
            output.writeByte(1);
            storePersistent(output, p);
        }
        output.writeByte(0);
    }

    @Override
    public void redo(IEngine iEngine) {
        Transaction transaction = new Transaction();
        copy(delete, transaction.getDelete());
        copy(save, transaction.getSave());
        copy(updateNew, transaction.getUpdate());
        setAttribute(transaction, iEngine);
    }

    private void setAttribute(Transaction transaction, IEngine iEngine) {
        Element element = iEngine.getElement(elementId);
        Attribute attribute = iEngine.getAttribute(attributeId);
        Object oldValue = engine.getAttribute(element, attribute);
        iEngine.setBinaryAttribute(elementId, attributeId, transaction);
        Object newValue = engine.getAttribute(element, attribute);

        if (element != null) {
            long qId = engine.getQualifierIdForElement(elementId);
            Qualifier q = engine.getQualifier(qId);
            if (q.getAttributeForName() == attributeId) {
                element.setName((newValue == null) ? "" : newValue.toString());
            }
        }

        AttributeEvent event = new AttributeEvent(engine, element, attribute,
                oldValue, newValue, true);
        engine.attributeChanged(event);
    }

    @Override
    public void undo(IEngine iEngine) {
        Transaction transaction = new Transaction();
        copy(delete, transaction.getSave());
        copy(save, transaction.getDelete());
        copy(updateOld, transaction.getUpdate());
        transaction.setRemoveBranchInfo(removeBranchInfo);
        setAttribute(transaction, iEngine);
    }
}

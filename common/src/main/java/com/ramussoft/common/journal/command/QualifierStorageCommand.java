package com.ramussoft.common.journal.command;

import java.io.IOException;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public abstract class QualifierStorageCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = 9043392449386256593L;

    public QualifierStorageCommand(JournaledEngine engine) {
        super(engine);
    }

    protected void storeQualifier(BinaryDataOutput output, Qualifier qualifier)
            throws IOException {
        output.writeLong(qualifier.getId());
        output.writeLong(qualifier.getAttributeForName());
        output.writeSwimedString(qualifier.getName());
        output.writeBoolean(qualifier.isSystem());
        storeAttributes(output, qualifier.getAttributes());
        storeAttributes(output, qualifier.getSystemAttributes());
    }

    private void storeAttributes(BinaryDataOutput output,
                                 List<Attribute> attributes) throws IOException {
        output.writeInt(attributes.size());
        for (Attribute attribute : attributes) {
            output.writeLong(attribute.getId());
        }
    }

    protected void loadQualifier(BinaryDataInput input, Qualifier qualifier)
            throws IOException {
        qualifier.setId(input.readLong());
        qualifier.setAttributeForName(input.readLong());
        qualifier.setName(input.readSwimedString());
        qualifier.setSystem(input.readBoolean());
        loadAttributes(input, qualifier.getAttributes());
        loadAttributes(input, qualifier.getSystemAttributes());
    }

    private void loadAttributes(BinaryDataInput input,
                                List<Attribute> attributes) throws IOException {
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            attributes.add(engine.getAttribute(input.readLong()));
        }
    }
}

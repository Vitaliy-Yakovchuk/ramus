package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public abstract class AttributeStorageCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = 8039107332963377351L;

    public AttributeStorageCommand(JournaledEngine engine) {
        super(engine);
    }

    protected void storeAttribute(BinaryDataOutput output, Attribute attribute)
            throws IOException {
        output.writeLong(attribute.getId());
        output.writeSwimedString(attribute.getName());
        output.writeSwimedString(attribute.getAttributeType().getPluginName());
        output.writeSwimedString(attribute.getAttributeType().getTypeName());
        output.writeBoolean(attribute.getAttributeType().isComparable());
    }

    protected void loadAttribute(BinaryDataInput input, Attribute attribute)
            throws IOException {
        attribute.setAttributeType(new AttributeType());
        attribute.setId(input.readLong());
        attribute.setName(input.readSwimedString());
        attribute.getAttributeType().setPluginName(input.readSwimedString());
        attribute.getAttributeType().setTypeName(input.readSwimedString());
        attribute.getAttributeType().setComparable(input.readBoolean());
    }

}

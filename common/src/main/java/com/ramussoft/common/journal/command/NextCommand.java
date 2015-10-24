package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class NextCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = 4136700981785197130L;

    private long value;

    private String sequence;

    public NextCommand(JournaledEngine engine) {
        super(engine);
    }

    public NextCommand(JournaledEngine engine, long value, String sequence) {
        super(engine);
        this.value = value;
        this.sequence = sequence;
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        value = input.readLong();
        sequence = input.readSwimedString();
    }

    @Override
    public void redo(IEngine engine) {
        while (value > engine.nextValue(sequence))
            ;
    }

    @Override
    public void undo(IEngine engine) {
        // no undo for sequence
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        output.writeLong(value);
        output.writeSwimedString(sequence);
    }

}

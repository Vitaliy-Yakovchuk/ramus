package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class EndUserTransactionCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = 4060273378683460182L;

    public EndUserTransactionCommand(JournaledEngine engine) {
        super(engine);
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
    }

    @Override
    public void redo(IEngine engine) {
    }

    @Override
    public void undo(IEngine engine) {
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
    }

    @Override
    public int getEngineId() {
        return 0;
    }
}

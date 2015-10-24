package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class StartUserTransactionCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = -6499951016036781544L;

    public StartUserTransactionCommand(JournaledEngine engine) {
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

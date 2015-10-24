package com.ramussoft.common.journal;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.journal.command.Command;

public class StopUndoPointCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = 7819057748715610463L;

    public StopUndoPointCommand(JournaledEngine engine) {
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

}

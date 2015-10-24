package com.ramussoft.common.journal.command;

import java.io.IOException;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.event.StreamEvent;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

public class SetStreamCommand extends Command {

    /**
     *
     */
    private static final long serialVersionUID = 6223846255871991106L;

    private byte[] oldBytes;

    private byte[] newBytes;

    private String path;

    public SetStreamCommand(JournaledEngine engine) {
        super(engine);
    }

    public SetStreamCommand(JournaledEngine engine, byte[] oldBytes,
                            byte[] newBytes, String path) {
        this(engine);
        this.oldBytes = oldBytes;
        this.newBytes = newBytes;
        this.path = path;
    }

    @Override
    public void readBody(BinaryDataInput input) throws IOException {
        path = input.readSwimedString();
        int length = input.readInt();
        if (length >= 0) {
            oldBytes = new byte[length];
            input.readFully(oldBytes);
        }
        length = input.readInt();
        if (length >= 0) {
            newBytes = new byte[length];
            input.readFully(newBytes);
        }
    }

    @Override
    public void redo(IEngine engine) {
        engine.setStream(path, newBytes);
        this.engine.streamUpdated(new StreamEvent(this.engine, path, newBytes,
                true));
    }

    @Override
    public void undo(IEngine engine) {
        engine.setStream(path, oldBytes);
        this.engine.streamUpdated(new StreamEvent(this.engine, path, oldBytes,
                true));
    }

    @Override
    public void writeBody(BinaryDataOutput output) throws IOException {
        output.writeSwimedString(path);
        if (oldBytes == null)
            output.writeInt(-1);
        else {
            output.writeInt(oldBytes.length);
            output.write(oldBytes);
        }

        if (newBytes == null)
            output.writeInt(-1);
        else {
            output.writeInt(newBytes.length);
            output.write(newBytes);
        }
    }

}

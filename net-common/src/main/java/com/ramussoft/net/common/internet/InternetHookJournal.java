package com.ramussoft.net.common.internet;

import com.ramussoft.common.journal.BinaryAccessFile;
import com.ramussoft.common.journal.Journal;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.journal.command.Command;
import com.ramussoft.common.journal.command.EndUserTransactionCommand;
import com.ramussoft.common.journal.command.StartUserTransactionCommand;

public abstract class InternetHookJournal extends Journal {

    private long cursor = 0;

    public InternetHookJournal(BinaryAccessFile accessFile) {
        super(accessFile, -1l);
    }

    public abstract void onRedo(byte[] bs);

    public abstract void onUndo(byte[] bs);

    @Override
    public void store(Command command) {
        super.store(command);
        onRepeat(command);
    }

    @Override
    public Command undo() {
        Command undo = super.undo();
        if (undo instanceof StartUserTransactionCommand) {
            try {
                long cursor = getPointer();
                byte[] bs = new byte[(int) (this.cursor - cursor)];
                accessFile.read(bs);
                accessFile.seek(cursor);
                this.cursor = cursor;
                onUndo(bs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return undo;
    }

    @Override
    public Command redo() {
        Command command = super.redo();
        onRepeat(command);
        return command;
    }

    private void onRepeat(Command command) {
        try {
            if (command instanceof EndUserTransactionCommand) {
                long cursor = getPointer();
                accessFile.seek(this.cursor);
                byte[] bs = new byte[(int) (cursor - this.cursor)];
                accessFile.read(bs);
                this.cursor = cursor;
                onRedo(bs);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void serverCopy(byte[] data, JournaledEngine engine) {
        try {
            long c1 = getPointer();
            accessFile.write(data, 4, data.length - 8);
            if (accessFile.length() > accessFile.getFilePointer()) {
                accessFile.setLength(accessFile.getFilePointer());
            }
            long c2 = getPointer();
            accessFile.seek(c1);
            while (getPointer() < c2)
                redo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package com.ramussoft.net.common.internet;

import java.util.Hashtable;

import com.ramussoft.common.journal.BinaryAccessFile;
import com.ramussoft.common.journal.Journal;
import com.ramussoft.common.journal.JournaledEngine;

public class InternetSyncJournal {

    private BinaryAccessFile accessFile;

    private Hashtable<Integer, JournaledEngine> engines = new Hashtable<Integer, JournaledEngine>();

    public InternetSyncJournal(BinaryAccessFile accessFile) {
        this.accessFile = accessFile;
    }

    public void redo(byte[] bs) {
        try {
            accessFile.seek(0);
            accessFile.write(bs);
            accessFile.seek(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Journal journal = new Journal(accessFile, -1l) {
            @Override
            protected JournaledEngine getEngine(int engineId) {
                return InternetSyncJournal.this.getEngine(engineId);
            }
        };

        journal.redoUserTransaction();
    }

    public void registerEngine(JournaledEngine engine) {
        engines.put(engine.getId(), engine);
    }

    private JournaledEngine getEngine(int engineId) {
        return engines.get(engineId);
    }

    public void undo(byte[] data) {
        try {
            accessFile.seek(0);
            accessFile.write(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Journal journal = new Journal(accessFile, -1l) {
            @Override
            protected JournaledEngine getEngine(int engineId) {
                return InternetSyncJournal.this.getEngine(engineId);
            }
        };

        journal.undoUserTransaction();
    }

}

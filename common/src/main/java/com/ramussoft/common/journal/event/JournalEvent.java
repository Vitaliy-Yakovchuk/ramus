package com.ramussoft.common.journal.event;

import java.io.Serializable;

import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.command.Command;

public class JournalEvent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2728595532470120825L;

    private transient Journaled journal;

    private Command command;

    private long index;

    public JournalEvent(Journaled journal, Command command, long index) {
        this.journal = journal;
        this.command = command;
        this.index = index;
    }

    public Journaled getJournal() {
        return journal;
    }

    public Command getCommand() {
        return command;
    }

    public long getIndex() {
        return index;
    }
}

package com.ramussoft.common.journal;

import com.ramussoft.common.journal.event.JournalListener;

public interface Journaled {

    boolean canUndo();

    boolean canRedo();

    void startUserTransaction();

    void commitUserTransaction();

    void rollbackUserTransaction();

    void redoUserTransaction();

    void undoUserTransaction();

    void addJournalListener(JournalListener listener);

    void removeJournalListener(JournalListener listener);

    JournalListener[] getJournalListeners();

    boolean isUserTransactionStarted();

    void close();

    void setEnable(boolean b);

    boolean isEnable();

    void setNoUndoPoint();

    long getBranch();
}

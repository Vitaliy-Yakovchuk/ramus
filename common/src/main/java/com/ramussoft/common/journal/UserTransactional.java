package com.ramussoft.common.journal;

import com.ramussoft.common.journal.event.JournalAdatper;
import com.ramussoft.common.journal.event.JournalListener;

public class UserTransactional extends JournalAdatper implements Journaled {

    private Journaled journal;

    public UserTransactional(Journaled journal) {
        this.journal = journal;
        journal.addJournalListener(this);
    }

    @Override
    public boolean canRedo() {
        return journal.canRedo();
    }

    @Override
    public boolean canUndo() {
        return journal.canUndo();
    }

    @Override
    public void commitUserTransaction() {
        if (!isUserTransactionStarted()) {
            throw new RuntimeException(
                    "Trying to end user transaction, but it is not started.");
        }
        journal.commitUserTransaction();
    }

    @Override
    public void redoUserTransaction() {
        journal.redoUserTransaction();
    }

    @Override
    public void rollbackUserTransaction() {
        journal.rollbackUserTransaction();
    }

    @Override
    public void startUserTransaction() {
        if (isUserTransactionStarted()) {
            System.err.println("User transaction already started");
            return;
        }
        journal.startUserTransaction();
    }

    @Override
    public void undoUserTransaction() {
        journal.undoUserTransaction();
    }

    @Override
    public void addJournalListener(JournalListener listener) {
        journal.addJournalListener(listener);
    }

    @Override
    public JournalListener[] getJournalListeners() {
        return journal.getJournalListeners();
    }

    @Override
    public void removeJournalListener(JournalListener listener) {
        journal.removeJournalListener(listener);
    }

    @Override
    public boolean isUserTransactionStarted() {
        return journal.isUserTransactionStarted();
    }

    @Override
    public void close() {
        journal.close();
    }

    @Override
    public boolean isEnable() {
        return journal.isEnable();
    }

    @Override
    public void setEnable(boolean b) {
        journal.setEnable(b);
    }

    @Override
    public void setNoUndoPoint() {
        journal.setNoUndoPoint();
    }

    @Override
    public long getBranch() {
        return journal.getBranch();
    }
}

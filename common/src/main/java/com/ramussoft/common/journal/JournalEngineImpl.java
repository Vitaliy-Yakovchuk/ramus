package com.ramussoft.common.journal;

import com.ramussoft.common.journal.event.JournalListener;

public class JournalEngineImpl implements Journaled {

    private final JournaledEngine engine;

    public JournalEngineImpl(JournaledEngine engine) {
        this.engine = engine;
    }

    public Journal getJournal() {
        return engine.getJournal();
    }

    @Override
    public boolean canUndo() {
        return getJournal().canUndo();
    }

    @Override
    public boolean canRedo() {
        return getJournal().canRedo();
    }

    @Override
    public void startUserTransaction() {
        getJournal().startUserTransaction();
    }

    @Override
    public void commitUserTransaction() {
        getJournal().commitUserTransaction();
    }

    @Override
    public void rollbackUserTransaction() {
        getJournal().rollbackUserTransaction();
    }

    @Override
    public void redoUserTransaction() {
        getJournal().redoUserTransaction();
    }

    @Override
    public void undoUserTransaction() {
        getJournal().undoUserTransaction();
    }

    @Override
    public void addJournalListener(JournalListener listener) {
        engine.getJournalFactory().addJournalListener(listener);
    }

    @Override
    public JournalListener[] getJournalListeners() {
        return engine.getJournalFactory().getJournalListeners();
    }

    @Override
    public void removeJournalListener(JournalListener listener) {
        engine.getJournalFactory().removeJournalListener(listener);
    }

    @Override
    public boolean isUserTransactionStarted() {
        return getJournal().isUserTransactionStarted();
    }

    @Override
    public void close() {
        engine.getJournalFactory().close();
    }

    @Override
    public void setEnable(boolean b) {
        throw new RuntimeException("Dinamic enability is strange???");
        // engine.getJournal().setEnable(b);
    }

    @Override
    public boolean isEnable() {
        return engine.getJournal().isEnable();
    }

    @Override
    public void setNoUndoPoint() {
        getJournal().setNoUndoPoint();
    }

    @Override
    public long getBranch() {
        return getJournal().getBranch();
    }

}

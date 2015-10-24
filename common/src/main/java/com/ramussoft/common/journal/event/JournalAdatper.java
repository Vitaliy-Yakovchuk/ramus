package com.ramussoft.common.journal.event;

public abstract class JournalAdatper implements JournalListener {

    @Override
    public void afterRedo(JournalEvent event) {
    }

    @Override
    public void afterStore(JournalEvent event) {
    }

    @Override
    public void afterUndo(JournalEvent event) {
    }

    @Override
    public void beforeStore(JournalEvent event) {
    }

}

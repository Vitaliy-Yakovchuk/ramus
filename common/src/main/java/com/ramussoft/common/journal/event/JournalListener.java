package com.ramussoft.common.journal.event;

import java.util.EventListener;

public interface JournalListener extends EventListener {

    void beforeStore(JournalEvent event);

    void afterStore(JournalEvent event);

    void afterUndo(JournalEvent event);

    void afterRedo(JournalEvent event);

}

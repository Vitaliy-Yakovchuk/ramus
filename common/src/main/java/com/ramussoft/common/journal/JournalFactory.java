package com.ramussoft.common.journal;

import com.ramussoft.common.journal.event.JournalListener;

public interface JournalFactory {

    /**
     * Return a journal for a branch. If journal is not exists it will be
     * created.
     */

    Journal getJournal(JournaledEngine engine, long branchId);

    /**
     * Close all created journals
     */
    void close();

    void addJournalListener(JournalListener listener);

    JournalListener[] getJournalListeners();

    void removeJournalListener(JournalListener listener);

}

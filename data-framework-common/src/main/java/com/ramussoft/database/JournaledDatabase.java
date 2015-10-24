package com.ramussoft.database;

import com.ramussoft.common.Qualifier;

public class JournaledDatabase extends MemoryDatabase {
    @SuppressWarnings({"deprecation", "unused"})
    public JournaledDatabase() {
        super();
        if (true)
            throw new RuntimeException("Not implementated yes");
        // while (journal.canRedo())
        // journal.redo();
        for (Qualifier qualifier : journaledEngine.getQualifiers()) {
            journaledEngine.updateElementNames(null, qualifier);
        }
        for (Qualifier qualifier : journaledEngine.getSystemQualifiers()) {
            journaledEngine.updateElementNames(null, qualifier);
        }
    }
}

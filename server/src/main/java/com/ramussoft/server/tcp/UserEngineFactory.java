package com.ramussoft.server.tcp;

import java.io.File;

import com.ramussoft.common.Engine;
import com.ramussoft.common.cached.CachedEngine;
import com.ramussoft.common.journal.DirectoryJournalFactory;
import com.ramussoft.common.journal.JournalEngineImpl;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.journal.SuperEngineFactory;

public class UserEngineFactory {

    private Engine engine;

    private String tmpPath;

    private DirectoryJournalFactory journalFactory;

    private Engine journaledEngine;

    public UserEngineFactory(RamusService service, String tmpPath) {
        this.tmpPath = tmpPath;
        journalFactory = createJournalFactory();

        service.setDirectoryJournalFactory(journalFactory);

        journaledEngine = service.getEngineFactory().createJournaledEngine(
                journalFactory);

        JournaledEngine journal;
        if (journaledEngine instanceof JournaledEngine) {
            journal = ((JournaledEngine) journaledEngine);
        } else {
            journal = ((JournaledEngine) ((CachedEngine) journaledEngine)
                    .getSource());
        }

        this.engine = (Engine) SuperEngineFactory.createTransactionalEngine(
                journaledEngine, new JournalEngineImpl(journal));
    }

    private DirectoryJournalFactory createJournalFactory() {
        String journalPath = tmpPath + File.separator
                + System.currentTimeMillis();
        File dir = new File(journalPath);
        dir.mkdirs();
        return new DirectoryJournalFactory(dir);
    }

    public Engine getEngine() {
        return engine;
    }

    public Engine getJournaledEngine() {
        return journaledEngine;
    }
}

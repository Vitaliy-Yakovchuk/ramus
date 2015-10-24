package com.ramussoft.net.common.internet;

import java.util.List;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.journal.JournalFactory;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.persistent.PersistentRow;

public class InternetEngine extends JournaledEngine {

    public InternetEngine(PluginFactory pluginFactory, IEngine deligate,
                          List<PersistentRow> rows, JournalFactory journalFactory,
                          AccessRules accessor) throws ClassNotFoundException {
        super(pluginFactory, deligate, rows, journalFactory, accessor);
        throw new RuntimeException("Not implementated yet");
    }

}

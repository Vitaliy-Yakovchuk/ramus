package com.ramussoft.common.journal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.swing.event.EventListenerList;

import com.ramussoft.common.journal.event.JournalListener;

public class DirectoryJournalFactory implements JournalFactory {

    private final File directory;

    private EventListenerList listenerList = new EventListenerList();

    private Hashtable<Long, Journal> journals = new Hashtable<Long, Journal>();

    public DirectoryJournalFactory(File directory) {
        this.directory = directory;
    }

    @Override
    public Journal getJournal(JournaledEngine engine, long branchId) {
        if (directory == null) {
            Journal journal = new Journal(null, -1l);
            journal.setEnable(false);
            return journal;
        }
        Journal journal = journals.get(branchId);
        if (journal == null) {
            try {
                journal = new Journal(new BinaryAccessFile(new File(directory,
                        "ramus-" + branchId + ".journal"), "rw"), branchId);
                journal.registerEngine(engine);
                journal.setEnable(true);
                journals.put(branchId, journal);
                for (JournalListener listener : getJournalListeners())
                    journal.addJournalListener(listener);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return journal;
    }

    @Override
    public void close() {
        for (Journal journal : journals.values())
            journal.close();
    }

    @Override
    public void addJournalListener(JournalListener listener) {
        listenerList.add(JournalListener.class, listener);
        for (Journal journal : journals.values())
            journal.addJournalListener(listener);
    }

    @Override
    public JournalListener[] getJournalListeners() {
        return listenerList.getListeners(JournalListener.class);
    }

    @Override
    public void removeJournalListener(JournalListener listener) {
        listenerList.remove(JournalListener.class, listener);
        for (Journal journal : journals.values())
            journal.removeJournalListener(listener);
    }

    /**
     * Load journals from files for recovery. Order journals by branch id
     */
    public Journal[] loadJournals(JournaledEngine engine) {
        class J implements Comparable<J> {
            long branch;
            Journal journal;

            public int compareTo(J o) {
                if (branch == o.branch)
                    return 0;
                if (branch < o.branch)
                    return -1;
                return 1;
            }

            ;
        }

        List<J> js = new ArrayList<J>();

        for (File file : directory.listFiles())
            if (file.isFile()) {
                String name = file.getName();
                String prefix = "ramus-";
                String suffix = ".journal";
                if (name.startsWith(prefix) && name.endsWith(suffix)) {
                    String number = name.substring(prefix.length(),
                            name.length() - suffix.length());
                    try {
                        J j = new J();
                        j.branch = Long.parseLong(number);
                        Journal journal = new Journal(new BinaryAccessFile(
                                file, "rw"), j.branch);
                        journal.registerEngine(engine);
                        journal.setEnable(true);
                        journals.put(j.branch, journal);
                        j.journal = journal;
                        js.add(j);
                    } catch (Exception e) {
                    }
                }
            }

        Collections.sort(js);

        Journal[] journals = new Journal[js.size()];
        for (int i = 0; i < journals.length; i++)
            journals[i] = js.get(i).journal;
        return journals;
    }

    public File getDirectory() {
        return directory;
    }
}

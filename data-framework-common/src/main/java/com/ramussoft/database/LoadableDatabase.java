package com.ramussoft.database;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;

public class LoadableDatabase extends MemoryDatabase {

    private File file;

    private String additionalSuits;

    public LoadableDatabase(File file, String additionalSuits) {
        this.file = file;
        this.additionalSuits = additionalSuits;
        super.createEngines();
    }

    @Override
    protected Collection<? extends PluginProvider> getAdditionalSuits() {
        List<PluginProvider> suits = new ArrayList<PluginProvider>(1);
        PluginFactory.loadAdditionalSuits(additionalSuits, suits);
        return suits;
    }

    @Override
    protected String getJournalDirectoryName(String s) {
        return null;
    }

    @Override
    protected File getFile() {
        return file;
    }

    @Override
    protected void createEngines() {
    }
}

package com.ramussoft.database;

import java.io.File;

public class FileDatabaseFactory {

    public static Database createDatabase() {
        return createDatabase(null);
    }

    public static Database createDatabase(final File file) {
        MemoryDatabase database = new MemoryDatabase() {
            @Override
            protected File getFile() {
                return file;
            }

        };
        return database;
    }
}

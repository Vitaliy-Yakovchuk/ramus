package com.ramussoft.demo;

import java.io.File;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

public final class YamlToRsf {

    private YamlToRsf() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: YamlToRsf <directory> <file.rsf>");
            System.exit(2);
            return;
        }

        File source = new File(args[0]);
        if (!source.isDirectory()) {
            System.err.println("Directory not found: " + source);
            System.exit(2);
            return;
        }
        File target = new File(args[1]);

        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(source);
        try {
            Engine engine = database.getEngine(null);
            ((FileIEngineImpl) engine.getDeligate()).saveToFile(target);
        } finally {
            database.close();
        }

        System.out.println("Written " + target.getAbsolutePath());
    }
}

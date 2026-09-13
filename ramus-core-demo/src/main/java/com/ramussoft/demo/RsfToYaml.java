package com.ramussoft.demo;

import java.io.File;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

public final class RsfToYaml {

    private RsfToYaml() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: RsfToYaml <file.rsf> <directory>");
            System.exit(2);
            return;
        }

        File source = new File(args[0]);
        if (!source.isFile()) {
            System.err.println("File not found: " + source);
            System.exit(2);
            return;
        }
        File target = new File(args[1]);

        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(source);
        try {
            Engine engine = database.getEngine(null);
            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            impl.saveProject(target);
            impl.close();
        } finally {
            database.close();
        }

        System.out.println("Written to " + target.getAbsolutePath());
    }
}

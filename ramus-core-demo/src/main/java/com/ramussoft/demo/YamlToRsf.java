package com.ramussoft.demo;

import java.io.File;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

/**
 * Складає {@code .rsf} з проєкту нового формату — зворотний бік
 * {@link RsfToYaml}.
 * <p>
 * Потрібен лише для сумісності зі старими збірками: сам застосунок працює з
 * каталогом проєкту напряму.
 *
 * <pre>
 * java -cp ... com.ramussoft.demo.YamlToRsf каталог проєкт.rsf
 * </pre>
 */
public final class YamlToRsf {

    private YamlToRsf() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Використання: YamlToRsf <каталог> <файл.rsf>");
            System.exit(2);
            return;
        }

        File source = new File(args[0]);
        if (!source.isDirectory()) {
            System.err.println("Не знайдено каталог: " + source);
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

        System.out.println("Записано " + target.getAbsolutePath());
    }
}

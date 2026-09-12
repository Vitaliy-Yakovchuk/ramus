package com.ramussoft.demo;

import java.io.File;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

/**
 * Конвертер {@code .rsf} у дерево YAML-файлів.
 * <p>
 * Потрібен, щоб перегнати наявні архіви користувачів у новий формат, не
 * піднімаючи застосунок. Запис детермінований, тож повторний запуск на тому
 * самому файлі дає той самий результат.
 *
 * <pre>
 * java -cp ... com.ramussoft.demo.RsfToYaml проєкт.rsf каталог-призначення
 * </pre>
 */
public final class RsfToYaml {

    private RsfToYaml() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Використання: RsfToYaml <файл.rsf> <каталог>");
            System.exit(2);
            return;
        }

        File source = new File(args[0]);
        if (!source.isFile()) {
            System.err.println("Не знайдено файл: " + source);
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

        System.out.println("Записано в " + target.getAbsolutePath());
    }
}

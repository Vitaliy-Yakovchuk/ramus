package com.ramussoft.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Перетворення шляху, який вибрав користувач, на каталог проєкту.
 * <p>
 * Це той шар, де стикаються три різні уявлення про «файл проєкту»: старий
 * архів, каталог і його опис. Помилка тут не падає, а тихо створює проєкт не
 * там, де очікує користувач.
 */
public class ProjectNamingTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void rsfBecomesProjectDirectory() throws Exception {
        File source = new File(folder.getRoot(), "Модель.rsf");
        assertEquals(new File(folder.getRoot(), "Модель.ramus"),
                FilePlugin.projectName(source));
    }

    @Test
    public void nameWithoutExtensionGetsOne() throws Exception {
        File source = new File(folder.getRoot(), "Модель");
        assertEquals(new File(folder.getRoot(), "Модель.ramus"),
                FilePlugin.projectName(source));
    }

    @Test
    public void projectDirectoryStaysAsItIs() throws Exception {
        File source = new File(folder.getRoot(), "Модель.ramus");
        assertEquals(source, FilePlugin.projectName(source));
    }

    /**
     * З робочого столу приходить опис проєкту, а не каталог.
     */
    @Test
    public void descriptionResolvesToItsDirectory() throws Exception {
        File project = folder.newFolder("Модель.ramus");
        File description = new File(project, "project.ramus");
        new FileWriter(description).close();

        assertEquals(project, FilePlugin.projectName(description));
        assertTrue(FilePlugin.isProject(description));
    }

    @Test
    public void plainDirectoryIsNotAProject() throws Exception {
        assertFalse(FilePlugin.isProject(folder.newFolder("просто-каталог")));
        assertFalse(FilePlugin.isProject(null));
    }
}

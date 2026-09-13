package com.ramussoft.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ProjectNamingTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void rsfBecomesProjectDirectory() throws Exception {
        File source = new File(folder.getRoot(), "Model.rsf");
        assertEquals(new File(folder.getRoot(), "Model.ramus"),
                FilePlugin.projectName(source));
    }

    @Test
    public void nameWithoutExtensionGetsOne() throws Exception {
        File source = new File(folder.getRoot(), "Model");
        assertEquals(new File(folder.getRoot(), "Model.ramus"),
                FilePlugin.projectName(source));
    }

    @Test
    public void projectDirectoryStaysAsItIs() throws Exception {
        File source = new File(folder.getRoot(), "Model.ramus");
        assertEquals(source, FilePlugin.projectName(source));
    }

    @Test
    public void descriptionResolvesToItsDirectory() throws Exception {
        File project = folder.newFolder("Model.ramus");
        File description = new File(project, "project.ramus");
        new FileWriter(description).close();

        assertEquals(project, FilePlugin.projectName(description));
        assertTrue(FilePlugin.isProject(description));
    }

    @Test
    public void plainDirectoryIsNotAProject() throws Exception {
        assertFalse(FilePlugin.isProject(folder.newFolder("plain-directory")));
        assertFalse(FilePlugin.isProject(null));
    }
}

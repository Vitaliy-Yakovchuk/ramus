package com.ramussoft.demo.sample1;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.database.Database;
import com.ramussoft.database.FileDatabaseFactory;

public class Application {

    public static final String QUALIFIER1 = "QUALIFIER1";

    public static final String TEXT_ATTRIBUTE1 = "TEXT_ATTRIBUTE1";

    public static final String TEXT_ATTRIBUTE2 = "TEXT_ATTRIBUTE2";

    public static final String DOUBLE_ATTRIBUTE1 = "DOUBLE_ATTRIBUTE1";

    public static void main(String[] args) {
        System.setProperty("user.ramus.application.name", "DemoApplication1");
        if (args.length == 0)
            if (JOptionPane.showConfirmDialog(null,
                    "Do you want to create a new file of your application?",
                    UIManager.getString("OptionPane.messageDialogTitle"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new MyFileFilter());
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    args = new String[]{chooser.getSelectedFile()
                            .getAbsolutePath()};
                }
            }
        new Application().run(args);
    }

    private void run(String[] args) {
        if (args.length == 1)
            openFile(new File(args[0]));
        else {
            System.err
                    .println("Usage:		Application file_name		To open a file.");
            System.out.println("Creating a new file");
            openFile(null);
        }

    }

    private void openFile(File file) {
        Database database = FileDatabaseFactory.createDatabase(file);
        patchData(database.getEngine(null));
        new MainFrame(database).setVisible(true);
    }

	/*
     * Create all needed system qualifiers and attributes.
	 */

    private void patchData(Engine engine) {
        Journaled journal = (Journaled) engine;
        journal.startUserTransaction();
        Attribute textAttribute1 = createAttribute(TEXT_ATTRIBUTE1,
                new AttributeType("Core", "Text"), engine);
        Attribute textAttribute2 = createAttribute(TEXT_ATTRIBUTE2,
                new AttributeType("Core", "Text"), engine);
        Attribute doubleAttribute1 = createAttribute(DOUBLE_ATTRIBUTE1,
                new AttributeType("Core", "Double"), engine);
        Qualifier qualifier1 = engine.getSystemQualifier(QUALIFIER1);
        if (qualifier1 == null) {
            qualifier1 = engine.createSystemQualifier();
            qualifier1.getAttributes().add(textAttribute1);
            qualifier1.getAttributes().add(textAttribute2);
            qualifier1.getAttributes().add(doubleAttribute1);
            qualifier1.setName(QUALIFIER1);
            engine.updateQualifier(qualifier1);
        }
        journal.commitUserTransaction();
        journal.setNoUndoPoint();// User will not be able to undo these changes.
    }

	/*
     * Create attribute if not exists.
	 */

    private Attribute createAttribute(String attributeName,
                                      AttributeType attributeType, Engine engine) {
        Attribute attribute = engine.getSystemAttribute(attributeName);
        if (attribute == null) {
            attribute = engine.createSystemAttribute(attributeType);
            attribute.setName(attributeName);
            engine.updateAttribute(attribute);
        }
        return attribute;
    }
}

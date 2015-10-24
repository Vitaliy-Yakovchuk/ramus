package com.ramussoft.demo;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.Database;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;

public class RSFViewer2 extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = -1699911394689870951L;

    private Engine engine;

    private JTree tree;

    public RSFViewer2(final Engine engine) {
        this.engine = engine;
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    ((FileIEngineImpl) engine.getDeligate()).close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
        this.setTitle("Ramus files viewer sample 2");

        JSplitPane pane = new JSplitPane();

        pane.setLeftComponent(createQualifiersList());
        pane.setRightComponent(createTreeView());

        this.setContentPane(pane);

        this.pack();
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
    }

    private Component createTreeView() {
        JScrollPane pane = new JScrollPane();

        tree = new JTree(new DefaultTreeModel(null));

        pane.setViewportView(tree);

        return pane;
    }

    private Component createQualifiersList() {

        // Load tree with elements meta data standard system qualifier.

        RowSet rowSet = new RowSet(engine, StandardAttributesPlugin
                .getQualifiersQualifier(engine), new Attribute[]{});

        final JTree qualifiersTree = new JTree(new DefaultTreeModel(rowSet
                .getRoot()));

        qualifiersTree.setRootVisible(false);

        qualifiersTree.getSelectionModel().addTreeSelectionListener(
                new TreeSelectionListener() {

                    RowSet rowSet = null;

                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        // Load elements of qualifier.
                        // We will load elements with RowSet class,
                        // you can also load elements with engne.getElements
                        // methods.
                        Object value = qualifiersTree.getSelectionPath()
                                .getLastPathComponent();
                        // Value is really a Row element.
                        if (value != null) {
                            if (rowSet != null) {// If rowSet is opened, we
                                // should close it and free
                                // all listeners.
                                rowSet.close();
                                rowSet = null;// Just in case :)
                            }
                            Row qualifierRow = (Row) value;
                            // Even you are not going to use any attribute,
                            // use RowSet(Engine, Qualifier Attribute[])
                            // constructor
                            // instead of RowSet(Engine, Qualifier) constructor.
                            rowSet = new RowSet(engine,
                                    StandardAttributesPlugin.getQualifier(
                                            engine, qualifierRow.getElement()),
                                    new Attribute[]{});

                            // And just load tree component with elements
                            // covered to Row objects.
                            tree
                                    .setModel(new DefaultTreeModel(rowSet
                                            .getRoot()));
                        }
                    }
                });

        JScrollPane pane = new JScrollPane();

        pane.setViewportView(qualifiersTree);

        return pane;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        System.setProperty("user.ramus.application.name", "RSFViewerDemo");
        // always set this value not to make conflicts with Ramus preferences
        // folder.

        if (args.length < 1) {
            System.err.println("Usage: RSFViewer rsf_file		View rsf file");
            System.out.println("Show open dialog");
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileFilter() {

                @Override
                public String getDescription() {
                    return "Ramus rsf files";
                }

                @Override
                public boolean accept(File f) {
                    if (f.isFile()) {
                        return f.getName().toLowerCase().endsWith(".rsf");
                    } else
                        return true;
                }
            });

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                args = new String[]{chooser.getSelectedFile()
                        .getAbsolutePath()};
            } else {
                System.exit(0);
                return;
            }
        }

        Database database = FileDatabaseFactory
                .createDatabase(new File(args[0]));

        new RSFViewer2(database.getEngine(null)).setVisible(true);
    }

}

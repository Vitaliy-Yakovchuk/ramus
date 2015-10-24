package com.ramussoft.demo;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.Database;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.common.RowSet;

public class RSFViewer extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = -1699911394689870951L;

    private Engine engine;

    private JTree tree;

    public RSFViewer(final Engine engine) {
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
        this.setTitle("Ramus files viewer sample 1");

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
        List<Qualifier> qualifiers = engine.getQualifiers();// Get qualifiers
        // list

        Collections.sort(qualifiers, new Comparator<Qualifier>() {// Sort
            // qualifiers
            // list by
            // name

            private Collator collator = Collator.getInstance();

            @Override
            public int compare(Qualifier o1, Qualifier o2) {
                return collator.compare(o1.getName(), o2.getName());
            }
        });

        final JList list = new JList(qualifiers.toArray());

        list.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    RowSet rowSet = null;

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        // Load elements of qualifier.
                        // We will load elements with RowSet class,
                        // you can also load elements with engne.getElements
                        // methods.
                        Object value = list.getSelectedValue();// Value is
                        // really a
                        // Qualifier
                        // object.
                        if (value != null) {
                            if (rowSet != null) {// If rowSet is opened, we
                                // should close it and free
                                // all listeners.
                                rowSet.close();
                                rowSet = null;// Just in case :)
                            }
                            Qualifier qualifier = (Qualifier) value;
                            // Even you are not going to use any attribute,
                            // use RowSet(Engine, Qualifier Attribute[])
                            // constructor
                            // instead of RowSet(Engine, Qualifier) constructor.
                            rowSet = new RowSet(engine, qualifier,
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

        pane.setViewportView(list);

        return pane;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        System.setProperty("user.ramus.application.name", "RSFViewerDemo");

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

        new RSFViewer(database.getEngine(null)).setVisible(true);
    }

}

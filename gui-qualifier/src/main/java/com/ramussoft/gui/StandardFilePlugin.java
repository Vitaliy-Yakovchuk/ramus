package com.ramussoft.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.ActionLevel;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.QualifierSelectComponent;

public class StandardFilePlugin extends AbstractViewPlugin {

    private PrintAction printAction = new PrintAction();

    private PageSetupAction pageSetupAction = new PageSetupAction();

    private PrintPreviewAction printPreviewAction = new PrintPreviewAction();

    private ImportQualifiersFromFileAction importQualifiersFromFileAction = new ImportQualifiersFromFileAction();

    private ExportQualifiersToFileAction exportQualifiersToFileAction = new ExportQualifiersToFileAction();

    public static final String ACTION_PRINT_PREVIEW = "Action.PrintPreview";

    public static final String ACTION_PRINT = "Action.Print";

    public static final String ACTION_PAGE_SETUP = "Action.PageSetup";

    @Override
    public String getName() {
        return "StandardFilePlugin";
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor exit = new ActionDescriptor();

        exit.setMenu("File");
        exit.setAction(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -4065282538673606022L;

            {
                putValue(ACTION_COMMAND_KEY, "Action.Exit");
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                        KeyEvent.VK_F4, KeyEvent.ALT_MASK));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                framework.exit();
            }
        });

        ActionDescriptor separator = new ActionDescriptor();
        separator.setMenu("File");

        ActionDescriptor printDescriptor = new ActionDescriptor();
        printDescriptor.setMenu("File");
        printDescriptor.setToolBar("Print");
        printDescriptor.setAction(printAction);
        printDescriptor.setActionLevel(ActionLevel.VIEW);

        ActionDescriptor pageSetupDescriptor = new ActionDescriptor();
        pageSetupDescriptor.setMenu("File");
        pageSetupDescriptor.setToolBar("Print");
        pageSetupDescriptor.setAction(pageSetupAction);
        pageSetupDescriptor.setActionLevel(ActionLevel.VIEW);

        ActionDescriptor printPreviewDescriptor = new ActionDescriptor();
        printPreviewDescriptor.setMenu("File");
        printPreviewDescriptor.setToolBar("Print");
        printPreviewDescriptor.setAction(printPreviewAction);
        printPreviewDescriptor.setActionLevel(ActionLevel.VIEW);

        ActionDescriptor importQualifiersDescriptor = new ActionDescriptor();
        importQualifiersDescriptor.setMenu("File");
        importQualifiersDescriptor.setAction(importQualifiersFromFileAction);

        ActionDescriptor exportQualifiersDescriptor = new ActionDescriptor();
        exportQualifiersDescriptor.setMenu("File");
        exportQualifiersDescriptor.setAction(exportQualifiersToFileAction);

        return new ActionDescriptor[]{importQualifiersDescriptor,
                exportQualifiersDescriptor, separator, pageSetupDescriptor,
                printPreviewDescriptor, printDescriptor, separator, exit};
    }

    public Action getPrintAction() {
        return printAction;
    }

    public Action getPageSetupAction() {
        return pageSetupAction;
    }

    public Action getPrintPreviewAction() {
        return printPreviewAction;
    }

    private class PrintPreviewAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5626942765570657353L;

        public PrintPreviewAction() {
            putValue(ACTION_COMMAND_KEY, ACTION_PRINT_PREVIEW);
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/print-preview.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            framework.propertyChanged((String) getValue(ACTION_COMMAND_KEY));
        }

    }

    ;

    private class PrintAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = 9176973290635083979L;

        public PrintAction() {
            putValue(ACTION_COMMAND_KEY, ACTION_PRINT);
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/print.png")));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
                    KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            framework.propertyChanged((String) getValue(ACTION_COMMAND_KEY));
        }

    }

    ;

    private class PageSetupAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -301046196414826014L;

        public PageSetupAction() {
            putValue(ACTION_COMMAND_KEY, ACTION_PAGE_SETUP);
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/page-setup.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            framework.propertyChanged((String) getValue(ACTION_COMMAND_KEY));
        }

    }

    ;

    private class ImportQualifiersFromFileAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5607956763151801720L;

        public ImportQualifiersFromFileAction() {
            putValue(ACTION_COMMAND_KEY, "Action.ImportQualifiers");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileFilter() {

                @Override
                public String getDescription() {
                    return "*.rsf";
                }

                @Override
                public boolean accept(File f) {
                    return (f.isDirectory())
                            || (f.getName().toLowerCase().endsWith(".rsf"));
                }
            });
            String file = Options.getString("LAST_FILE");
            if (file != null) {
                chooser.setSelectedFile(new File(file));
            }
            int r = chooser.showOpenDialog(framework.getMainFrame());
            if (r == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                importQualifiersFromFile(f);
            }

        }

        private void importQualifiersFromFile(final File file) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        framework.showAnimation(MessageFormat.format(
                                GlobalResourcesManager
                                        .getString("Message.Loading"), file
                                        .getName()));
                        MemoryDatabase md = new MemoryDatabase() {

                            protected String getJournalDirectoryName(String tmp) {
                                return null;
                            }

                            ;

                            @Override
                            protected File getFile() {
                                return file;
                            }

                        };
                        Engine sourceEngine = md.getEngine("ramus");

                        QualifierSelectComponent component = new QualifierSelectComponent(
                                sourceEngine, false, true);

                        framework.hideAnimation();

                        ButtonGroup group = new ButtonGroup();

                        JRadioButton update = new JRadioButton(
                                GlobalResourcesManager
                                        .getString("ImportQualifiersIfExistsUpdate"));
                        JRadioButton skip = new JRadioButton(
                                GlobalResourcesManager
                                        .getString("ImportQualifiersIfExistsSkip"));

                        JCheckBox importTables = new JCheckBox(
                                GlobalResourcesManager
                                        .getString("ImportTables"));

                        group.add(update);
                        group.add(skip);

                        update.setSelected(true);
                        JPanel jPanel = new JPanel(new FlowLayout(
                                FlowLayout.LEFT));
                        jPanel
                                .setBorder(BorderFactory
                                        .createTitledBorder(GlobalResourcesManager
                                                .getString("ImportQualifiersIfExists")));

                        jPanel.add(update);
                        jPanel.add(skip);
                        jPanel.add(importTables);

                        List<Qualifier> qualifiers = component.showDialog(
                                framework.getMainFrame(), jPanel,
                                BorderLayout.SOUTH);

                        List<Row> rows = component.getSelectedRows();

                        if (qualifiers.size() > 0) {

                            Engine engine = framework.getEngine();

                            QualifierImporterImpl qualifierImporter = new QualifierImporterImpl(
                                    sourceEngine, engine, framework, qualifiers
                                    .toArray(new Qualifier[qualifiers
                                            .size()]), rows
                                    .toArray(new Row[rows.size()]));

                            ((Journaled) engine).startUserTransaction();
                            try {
                                framework.showAnimation(GlobalResourcesManager
                                        .getString("Wait.QualifierImporting"));
                                qualifierImporter
                                        .importQualifiers(
                                                (update.isSelected()) ? QualifierImporterImpl.ELEMENT_IMPORT_TYPE_UPDATE
                                                        : QualifierImporterImpl.ELEMENT_IMPORT_TYPE_SKIP,
                                                importTables.isSelected());

                                ((Journaled) engine).commitUserTransaction();
                            } catch (Exception e) {
                                e.printStackTrace();
                                ((Journaled) engine).rollbackUserTransaction();
                                FileIEngineImpl impl = (FileIEngineImpl) sourceEngine
                                        .getDeligate();
                                impl.close();
                                throw e;
                            } finally {
                                framework.hideAnimation();
                            }
                        }

                        FileIEngineImpl impl = (FileIEngineImpl) sourceEngine
                                .getDeligate();
                        impl.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        framework.hideAnimation();
                        JOptionPane.showMessageDialog(framework.getMainFrame(),
                                e.getLocalizedMessage());
                    }

                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

    }

    ;

    private class ExportQualifiersToFileAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5607956763151801720L;

        public ExportQualifiersToFileAction() {
            putValue(ACTION_COMMAND_KEY, "Action.ExportQualifiers");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser() {
                /**
                 *
                 */
                private static final long serialVersionUID = -2539273621585333693L;

                @Override
                public void approveSelection() {
                    if (getSelectedFile().exists()) {
                        if (JOptionPane.showConfirmDialog(framework
                                        .getMainFrame(), GlobalResourcesManager
                                        .getString("File.Exists"), UIManager
                                        .getString("OptionPane.messageDialogTitle"),
                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                            return;
                    }
                    super.approveSelection();
                }
            };
            chooser.setFileFilter(new FileFilter() {

                @Override
                public String getDescription() {
                    return "*.rsf";
                }

                @Override
                public boolean accept(File f) {
                    return (f.isDirectory())
                            || (f.getName().toLowerCase().endsWith(".rsf"));
                }
            });

            int r = chooser.showSaveDialog(framework.getMainFrame());
            if (r == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".rsf"))
                    f = new File(f.getParentFile(), f.getName() + ".rsf");
                exportQualifiersToFile(f);
            }

        }

        private void exportQualifiersToFile(final File file) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {

                        QualifierSelectComponent component = new QualifierSelectComponent(
                                framework.getEngine(), false, true);

                        List<Qualifier> qualifiers = component.showDialog(
                                framework.getMainFrame(), null, null);

                        List<Row> rows = component.getSelectedRows();

                        if (qualifiers.size() > 0) {

                            MemoryDatabase md = new MemoryDatabase() {

                                protected String getJournalDirectoryName(String tmp) {
                                    return null;
                                }

                                @Override
                                protected File getFile() {
                                    return null;
                                }

                            };
                            Engine destinationEngine = md.getEngine("ramus");

                            Engine engine = framework.getEngine();

                            QualifierImporterImpl qualifierImporter = new QualifierImporterImpl(
                                    engine, destinationEngine, framework,
                                    qualifiers.toArray(new Qualifier[qualifiers
                                            .size()]), rows
                                    .toArray(new Row[rows.size()]));

                            ((Journaled) destinationEngine)
                                    .startUserTransaction();
                            try {
                                framework.showAnimation(GlobalResourcesManager
                                        .getString("Wait.QualifierExporting"));
                                qualifierImporter
                                        .importQualifiers(
                                                QualifierImporterImpl.ELEMENT_IMPORT_TYPE_UPDATE,
                                                true);

                                ((Journaled) destinationEngine)
                                        .commitUserTransaction();
                                FileIEngineImpl impl = (FileIEngineImpl) destinationEngine
                                        .getDeligate();
                                impl.saveToFile(file);
                            } catch (Exception e) {
                                ((Journaled) destinationEngine)
                                        .rollbackUserTransaction();
                                FileIEngineImpl impl = (FileIEngineImpl) destinationEngine
                                        .getDeligate();
                                impl.close();
                                throw e;
                            } finally {
                                framework.hideAnimation();
                            }
                            FileIEngineImpl impl = (FileIEngineImpl) destinationEngine
                                    .getDeligate();
                            impl.close();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(framework.getMainFrame(),
                                e.getLocalizedMessage());
                    }

                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

    }

    ;
}

/*
 * Created on 4/8/2005
 */
package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.DataLoader;
import com.dsoft.utils.Options;
import com.ramussoft.common.AccessRules;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.frames.BaseDialog;
import com.ramussoft.pb.frames.MainFrame;
import com.ramussoft.pb.frames.components.ColorChooser16;
import com.ramussoft.pb.frames.components.JFontChooser;
import com.ramussoft.pb.frames.components.LineStyleChooser;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.visual.MovingArea;

/**
 * @author ZDD
 */
public class ArrowOptionstDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private javax.swing.JPanel jContentPane = null;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private JTabbedPane jTabbedPane = null;

    private JPanel jPanel4 = null;

    private JPanel jPanel5 = null;

    private JFontChooser jFontChooser = null;

    public boolean isOk;

    private JPanel jPanel8 = null;

    private JPanel jPanel9 = null;

    private JPanel jPanel10 = null;

    private JLabel jLabel2 = null;

    private JScrollPane jScrollPane = null;

    private JScrollPane jScrollPane1 = null;

    private JLabel jLabel3 = null;

    private JTextPane jTextArea = null;

    private JTextPane jTextArea1 = null;

    private ColorChooser16 colorChooser16 = null;

    private JPanel jPanel11 = null;

    private JLabel jLabel4 = null;

    private LineStyleChooser lineStyleChooser = null;

    private JPanel sectorOption = null;

    private SectorRowsEditor sectorRowsEditor = null;

    private SectorNameEditor sectorNameEditor = null;

    private DataPlugin dataPlugin;

    private GUIFramework framework;

    private AccessRules accessRules;

    private JPanel sectorNameOption;

    private boolean isCanOk() {
        final Row[] rs = sectorRowsEditor.getRows();
        final boolean b = rs.length > 0
                || !sectorNameEditor.getArrowName().equals("");
        return b;
    }

    public void showModal(final PaintSector sector, MovingArea movingArea) {
        isOk = false;
        Options.loadOptions("select_row", this);
        Options.saveOptions("select_row", this);
        sectorRowsEditor.setSector(sector.getSector());
        sectorNameEditor.setSector(sector.getSector());
        sectorRowsEditor.setSectorNameEditor(sectorNameEditor);
        getColorChooser16().setColor(sector.getColor());
        getJFontChooser().setSelFont(sector.getFont());
        final Stream s = sector.getStream();
        loadFrom(s);
        lineStyleChooser.setStroke(sector.getStroke());
        sectorNameEditor.beforeShow();
        Options.loadOptions("select_row", this);

        setVisible(true);
        if (isOk) {
            ((NSector) sector.getSector()).getDataPlugin()
                    .startUserTransaction();
            sector.setFont(getJFontChooser().getSelFont());
            sector.setColor(getColorChooser16().getColor());
            sector.setStroke(lineStyleChooser.getStroke());
            if (lineStyleChooser.isDefaultArrowStyle()) {
                Options.setStroke("DEFAULT_ARROW_STROKE",
                        lineStyleChooser.getStroke());
            }
            sector.copyVisual(Sector.VISUAL_COPY_ADDED);
            sector.setShowText(sectorNameEditor.getBox().isSelected());
            sector.setAlternativeText(sectorNameEditor
                    .getAlternativeTextField().getText());
            sector.getSector().setTextAligment(sectorNameEditor.getTextAligment());
            Stream stream = sectorRowsEditor.getStream();

            if (stream == null) {
                stream = sectorNameEditor.findStreamByName();
                if (stream == null) {
                    stream = (Stream) dataPlugin.createRow(
                            dataPlugin.getBaseStream(), true);
                }
            }

            final String t = sectorNameEditor.getArrowName();
            if (!t.equals("")) {
                if (!t.equals(stream.getName())
                        && sectorNameEditor.findStreamByName(stream.getName()) != null) {
                    stream = (Stream) dataPlugin.createRow(
                            dataPlugin.getBaseStream(), true);
                }
            }
            if (t.equals("")) {
                if (!stream.isEmptyName()) {
                    stream = (Stream) dataPlugin.createRow(
                            dataPlugin.getBaseStream(), true);
                    stream.setEmptyName(true);

                }
            } else {
                stream.setName(t);
            }
            final Row[] added = stream.getAdded();
            stream.setRows(sectorRowsEditor.getRows());
            sector.setStream(stream, sectorNameEditor.getReplaceStreamType());
            stream = sector.getStream();
            stream.setRows(added);
            saveTo(stream);
            // sector.setStreamAddedByRefactor(false);
            final Row[] rs = sectorRowsEditor.getRows();
            sector.setRows(rs);

            sector.createTexts();
            HashSet<PaintSector> hashSet = new HashSet();
            sector.getConnectedSector(hashSet);
            for (PaintSector sp : hashSet)
                PaintSector.save(sp, new DataLoader.MemoryData(),
                        dataPlugin.getEngine());
            movingArea.getRefactor().setUndoPoint();
        }

        saveOption();
    }

    private void saveTo(final Stream stream) {

    }

    private void loadFrom(final Stream s) {
        if (s != null) {
        } else {
            getJTextArea().setText("");
            getJTextArea1().setText("");
        }
    }

    /**
     * This is the default constructor
     */
    public ArrowOptionstDialog(DataPlugin dataPlugin, GUIFramework framework,
                               AccessRules accessRules) {
        super();
        this.setIconImage(MainFrame.mainIcon);
        this.dataPlugin = dataPlugin;
        this.accessRules = accessRules;
        this.framework = framework;
        setModal(true);
        initialize();
        setLocationRelativeTo(null);
    }

    public ArrowOptionstDialog(final JFrame frame, DataPlugin dataPlugin,
                               GUIFramework framework, AccessRules accessRules) {
        super(frame, true);
        this.setIconImage(MainFrame.mainIcon);
        this.dataPlugin = dataPlugin;
        this.accessRules = accessRules;
        this.framework = framework;
        initialize();
        setLocationRelativeTo(frame);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setResizable(true);
        // this.setModal(true);
        setTitle("arrow_options");
        setMainPane(getJContentPane());
        pack();
        setMinimumSize(getSize());
        ResourceLoader.setJComponentsText(this);
    }

    /**
     *
     */
    protected void saveOption() {
        Options.saveOptions("select_row", this);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            final FlowLayout flowLayout1 = new FlowLayout();
            jPanel = new JPanel();
            jPanel.setLayout(flowLayout1);
            flowLayout1.setAlignment(java.awt.FlowLayout.RIGHT);
            jPanel.add(getJPanel1(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            final GridLayout gridLayout2 = new GridLayout();
            jPanel1 = new JPanel();
            jPanel1.setLayout(gridLayout2);
            gridLayout2.setRows(1);
            gridLayout2.setHgap(5);
            jPanel1.add(getJButton(), null);
            jPanel1.add(getJButton1(), null);
        }
        return jPanel1;
    }

    private boolean isOkStreamName() {
        final Stream stream = sectorNameEditor.findStreamByName();
        if (stream != null)
            return stream.equals(sectorNameEditor.getNullStream());
        return true;
    }

    @Override
    protected void onOk() {
        if (!isCanOk()) {
            JOptionPane
                    .showMessageDialog(
                            this,
                            ResourceLoader
                                    .getString("you_should_enter_name_or_at_least_on_added_elemen"));
            return;
        }
        if (!isOkStreamName()) {
            if (JOptionPane.showConfirmDialog(this, ResourceLoader
                            .getString("you_entered_exists_stream_continue"),
                    ResourceLoader.getString("warning"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            } else
                loadFrom(sectorNameEditor.findStreamByName());

        }
        isOk = true;
        super.onOk();
    }

    /**
     * This method initializes jTabbedPane
     *
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.addTab(ResourceLoader.getString("stream"), null,
                    getSectorOption(), null);
            jTabbedPane.addTab(ResourceLoader.getString("name"), null,
                    getSectorNameOption(), null);
            jTabbedPane.addTab(ResourceLoader.getString("color"), null,
                    getJPanel5(), null);
            jTabbedPane.addTab(ResourceLoader.getString("font"), null,
                    getJPanel4(), null);
            jTabbedPane.addTab(ResourceLoader.getString("arrow"), null,
                    getJPanel8(), null);
        }
        return jTabbedPane;
    }

    /**
     * This method initializes jPanel4
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel4() {
        if (jPanel4 == null) {
            jPanel4 = new JPanel();
            jPanel4.setLayout(new BorderLayout());
            jPanel4.add(getJFontChooser(), java.awt.BorderLayout.CENTER);
        }
        return jPanel4;
    }

    /**
     * This method initializes jPanel5
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel5() {
        if (jPanel5 == null) {
            jPanel5 = new JPanel();
            jPanel5.setLayout(new BorderLayout());
            jPanel5.add(getColorChooser16(), java.awt.BorderLayout.CENTER);
        }
        return jPanel5;
    }

    /**
     * This method initializes jFontChooser
     *
     * @return com.jason.clasificators.frames.idf.JFontChooser
     */
    private JFontChooser getJFontChooser() {
        if (jFontChooser == null) {
            jFontChooser = new JFontChooser();
        }
        return jFontChooser;
    }

    /**
     * This method initializes jPanel8
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel8() {
        if (jPanel8 == null) {
            final GridLayout gridLayout1 = new GridLayout();
            jPanel8 = new JPanel();
            jPanel8.setPreferredSize(new Dimension(100, 100));
            jPanel8.setLayout(gridLayout1);
            gridLayout1.setColumns(1);
            gridLayout1.setRows(3);
            jPanel8.add(getJPanel11(), null);
            jPanel8.add(getJPanel9(), null);
            jPanel8.add(getJPanel10(), null);
        }
        return jPanel8;
    }

    /**
     * This method initializes jPanel9
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel9() {
        if (jPanel9 == null) {
            jLabel2 = new JLabel();
            jPanel9 = new JPanel();
            jPanel9.setLayout(new BorderLayout());
            jLabel2.setText("describe:");
            final FlowLayout layout = new FlowLayout();
            layout.setAlignment(FlowLayout.LEFT);
            layout.setHgap(0);
            final JPanel p = new JPanel(layout);
            p.add(jLabel2);
            jPanel9.add(p, java.awt.BorderLayout.NORTH);
            jPanel9.add(getJScrollPane1(), java.awt.BorderLayout.CENTER);
        }
        return jPanel9;
    }

    /**
     * This method initializes jPanel10
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel10() {
        if (jPanel10 == null) {
            jLabel3 = new JLabel();
            jPanel10 = new JPanel();
            jPanel10.setLayout(new BorderLayout());
            jLabel3.setText("note:");
            final FlowLayout layout = new FlowLayout();
            layout.setAlignment(FlowLayout.LEFT);
            layout.setHgap(0);
            final JPanel p = new JPanel(layout);
            p.add(jLabel3);
            jPanel10.add(p, java.awt.BorderLayout.NORTH);
            jPanel10.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return jPanel10;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextArea1());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jScrollPane1
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane1() {
        if (jScrollPane1 == null) {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setViewportView(getJTextArea());
        }
        return jScrollPane1;
    }

    /**
     * This method initializes jTextArea
     *
     * @return javax.swing.JTextArea
     */
    private JTextPane getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextPane();
            jTextArea.setContentType("text/html");
        }
        return jTextArea;
    }

    /**
     * This method initializes jTextArea1
     *
     * @return javax.swing.JTextArea
     */
    private JTextPane getJTextArea1() {
        if (jTextArea1 == null) {
            jTextArea1 = new JTextPane();
            jTextArea1.setContentType("text/html");
        }
        return jTextArea1;
    }

    /**
     * This method initializes colorChooser16
     *
     * @return com.jason.clasificators.frames.ColorChooser16
     */
    private ColorChooser16 getColorChooser16() {
        if (colorChooser16 == null) {
            colorChooser16 = new ColorChooser16();
        }
        return colorChooser16;
    }

    /**
     * This method initializes jPanel11
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel11() {
        if (jPanel11 == null) {
            jLabel4 = new JLabel();
            jPanel11 = new JPanel();
            jPanel11.setLayout(new BorderLayout());
            jLabel4.setText("line_type:");
            jPanel11.add(jLabel4, java.awt.BorderLayout.NORTH);
            jPanel11.add(getLineStyleChooser(), java.awt.BorderLayout.CENTER);
        }
        return jPanel11;
    }

    /**
     * This method initializes lineStyleChooser
     *
     * @return com.jason.clasificators.frames.idf.LineStyleChooser
     */
    private LineStyleChooser getLineStyleChooser() {
        if (lineStyleChooser == null) {
            lineStyleChooser = new LineStyleChooser();
        }
        return lineStyleChooser;
    }

    /**
     * This method initializes sectorOption
     *
     * @return javax.swing.JPanel
     */
    private JPanel getSectorOption() {
        if (sectorOption == null) {
            sectorOption = new JPanel();
            sectorOption.setLayout(new BorderLayout());
            sectorOption.add(getSectorRowsEditor(),
                    java.awt.BorderLayout.CENTER);
        }
        return sectorOption;
    }

    private JPanel getSectorNameOption() {
        if (sectorNameOption == null) {
            sectorNameOption = new JPanel();
            sectorNameOption.setLayout(new BorderLayout());
            sectorNameOption.add(getSectorNameEditor(),
                    java.awt.BorderLayout.CENTER);
        }
        return sectorNameOption;
    }

    /**
     * This method initializes sectorRowsEditor
     *
     * @return com.jason.clasificators.idef.frames.SectorRowsEditor
     */
    private SectorRowsEditor getSectorRowsEditor() {
        if (sectorRowsEditor == null) {
            sectorRowsEditor = new SectorRowsEditor(dataPlugin, framework,
                    accessRules) {
                @Override
                protected boolean selectRows() {
                    final boolean res = super.selectRows();
                    if (res)
                        loadFrom(getStream());
                    return res;
                }
            };
            sectorRowsEditor.setDialog(this);
            sectorRowsEditor.setPreferredSize(new Dimension(100, 100));
        }
        return sectorRowsEditor;
    }

    private SectorNameEditor getSectorNameEditor() {
        if (sectorNameEditor == null) {
            sectorNameEditor = new SectorNameEditor(dataPlugin, framework,
                    accessRules) {
                @Override
                protected boolean selectRows() {
                    final boolean res = super.selectRows();
                    if (res)
                        loadFrom(getStream());
                    return res;
                }

                @Override
                public Stream getStream() {
                    return sectorRowsEditor.getStream();
                }
            };
            sectorNameEditor.setDialog(this);
            sectorNameEditor.setPreferredSize(new Dimension(100, 100));
        }
        return sectorNameEditor;
    }

    public void close() {
        sectorRowsEditor.dispose();
    }

    /**
     * This method initializes jPanel6
     *
     * @return javax.swing.JPanel
     */

} // @jve:decl-index=0:visual-constraint="35,15"

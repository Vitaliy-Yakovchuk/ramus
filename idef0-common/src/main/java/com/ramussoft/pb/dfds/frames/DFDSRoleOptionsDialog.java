package com.ramussoft.pb.dfds.frames;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.RectangleVisualOptions;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.dfd.visual.DFDObject;
import com.ramussoft.pb.dfd.visual.DFDObjectDialog;
import com.ramussoft.pb.dfds.visual.DFDSRole;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.idef.frames.SectorNameEditor;
import com.ramussoft.pb.idef.frames.SectorRowsEditor;

public class DFDSRoleOptionsDialog extends DFDObjectDialog {

    private SectorRowsEditor sectorRowsEditor;

    private SectorNameEditor sectorNameEditor;

    public DFDSRoleOptionsDialog(GUIFramework framework, DataPlugin dataPlugin) {
        super(framework, dataPlugin);
    }

    private boolean isCanOk() {
        final Row[] rs = sectorRowsEditor.getRows();
        final boolean b = rs.length > 0
                || !sectorNameEditor.getArrowName().equals("");
        return b;
    }

    private boolean isOkStreamName() {
        final Stream stream = sectorNameEditor.findStreamByName();
        if (stream != null)
            return stream.equals(sectorNameEditor.getNullStream());
        return true;
    }

    @Override
    protected Component createFirstTab(JTabbedPane pane) {
        sectorRowsEditor = new SectorRowsEditor(dataPlugin, framework,
                framework.getAccessRules());
        sectorNameEditor = new SectorNameEditor(dataPlugin, framework,
                framework.getAccessRules()) {
            @Override
            protected void createReplacementPanel(JPanel ignoreMe) {
            }

            @Override
            public Stream getStream() {
                return sectorRowsEditor.getStream();
            }
        };
        sectorNameEditor.box.setVisible(false);
        sectorRowsEditor.setSectorNameEditor(sectorNameEditor);
        pane.addTab(ResourceLoader.getString("name"), sectorRowsEditor);
        return sectorNameEditor;
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
            }
        }

        DFDSRole role = (DFDSRole) object;

        Journaled journaled = (Journaled) dataPlugin.getEngine();
        journaled.startUserTransaction();
        Function function = object.getFunction();
        function.setFont(fontChooser.getSelFont());
        function.setBackground(backgroundColorChooser.getColor());
        function.setForeground(foregroundColorChooser.getColor());

        role.setAlternativeText(sectorNameEditor.getAlternativeTextField()
                .getText());
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
        stream.setRows(sectorRowsEditor.getRows());
        role.setStream(stream, ReplaceStreamType.SIMPLE);
        // sector.setStreamAddedByRefactor(false);
        final Row[] rs = sectorRowsEditor.getRows();
        role.setRows(rs);

        if (rs != null) {
            RectangleVisualOptions ops = new RectangleVisualOptions();
            ops.bounds = function.getBounds();
            ops.background = function.getBackground();
            ops.font = function.getFont();
            ops.foreground = function.getForeground();
            for (Row row : rs)
                if (row != null) {
                    IDEF0Plugin.setDefaultRectangleVisualOptions(
                            dataPlugin.getEngine(), row.getElement(), ops);
                }
        }

        journaled.commitUserTransaction();
        setVisible(false);
    }

    @Override
    public void showModal(DFDObject object) {
        sectorRowsEditor.setDFDSRole((DFDSRole) object);
        super.showModal(object);
    }
}

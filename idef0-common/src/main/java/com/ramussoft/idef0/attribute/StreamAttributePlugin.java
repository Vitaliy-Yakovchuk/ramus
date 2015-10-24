package com.ramussoft.idef0.attribute;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.table.TableCellEditor;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.DataLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.Closeable;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.frames.SectorNameEditor;
import com.ramussoft.pb.idef.frames.SectorRowsEditor;
import com.ramussoft.pb.types.GlobalId;

public class StreamAttributePlugin extends AbstractAttributePlugin implements
        TabledAttributePlugin {

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "OtherElement");
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return null;
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, final Element element,
                                              final Attribute attribute, AttributeEditor old) {

        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private SectorRowsEditor sectorRowsEditor;

            private SectorNameEditor sectorNameEditor;

            private PaintSector.Pin pin;

            private DataPlugin dataPlugin;

            private PaintSector sector;

            private JTabbedPane component;

            {
                dataPlugin = NDataPluginFactory.getDataPlugin(null, engine,
                        rules);
                Sector sector = dataPlugin.findSectorByGlobalId(GlobalId
                        .create(element.getId()));
                if (sector != null) {

                    dataPlugin = NDataPluginFactory.getDataPlugin(
                            engine.getQualifier(sector.getFunction()
                                    .getElement().getQualifierId()), engine,
                            rules);

                    component = new JTabbedPane();

                    sectorRowsEditor = new SectorRowsEditor(dataPlugin,
                            framework, rules);

                    sectorNameEditor = new SectorNameEditor(dataPlugin,
                            framework, rules) {

                        @Override
                        public Stream getStream() {
                            return sectorRowsEditor.getStream();
                        }
                    };

                    sectorRowsEditor.setSectorNameEditor(sectorNameEditor);

                    component.addTab(ResourceLoader.getString("stream"), null,
                            sectorRowsEditor, null);
                    component.addTab(ResourceLoader.getString("name"), null,
                            sectorNameEditor, null);

                    ResourceLoader.setJComponentsText(sectorRowsEditor);
                    ResourceLoader.setJComponentsText(sectorNameEditor);
                }
            }

            @Override
            public Object setValue(Object value) {
                this.pin = (PaintSector.Pin) value;
                sector = pin.getSector();
                sectorNameEditor.setSector(sector.getSector());
                sectorRowsEditor.setSector(sector.getSector());
                return value;
            }

            @Override
            public Object getValue() {
                return pin;
            }

            @Override
            public JComponent getComponent() {
                if (component == null)
                    return new JPanel();
                return component;
            }

            @Override
            public JComponent getLastComponent() {
                return component;
            }

            @Override
            public void apply(Engine engine, Element element,
                              Attribute attribute, Object value) {
                sector.setShowText(sectorNameEditor.getBox().isSelected());
                sector.setAlternativeText(sectorNameEditor
                        .getAlternativeTextField().getText());
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
                            && sectorNameEditor.findStreamByName(stream
                            .getName()) != null) {
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
                } else
                    stream.setName(t);
                final Row[] added = stream.getAdded();
                stream.setRows(sectorRowsEditor.getRows());
                sector.setStream(stream,
                        sectorNameEditor.getReplaceStreamType());
                stream = sector.getStream();
                stream.setRows(added);
                // sector.setStreamAddedByRefactor(false);
                final Row[] rs = sectorRowsEditor.getRows();
                sector.setRows(rs);

                sector.createTexts();

                PaintSector.save(sector, new DataLoader.MemoryData(), engine);

                sector.getMovingArea().getRefactor().setUndoPoint();
                sectorNameEditor.setChanged(false);
            }

            @Override
            public boolean canApply() {
                final boolean b = isSave();
                if (b) {
                    if (!isOkStreamName()) {
                        if (JOptionPane
                                .showConfirmDialog(
                                        component,
                                        ResourceLoader
                                                .getString("you_entered_exists_stream_continue"),
                                        ResourceLoader.getString("warning"),
                                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                            return false;

                    }
                } else {
                    JOptionPane
                            .showMessageDialog(
                                    component,
                                    ResourceLoader
                                            .getString("you_should_enter_name_or_at_least_on_added_elemen"));
                    return false;
                }
                return b;
            }

            private boolean isSave() {
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
            public boolean isSaveAnyway() {
                return sectorRowsEditor.isChanged()
                        || sectorNameEditor.isChanged();
            }

            @Override
            public void close() {
                super.close();
                sectorRowsEditor.dispose();
            }
        };
    }

    @Override
    public boolean isCellEditable() {
        return false;
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    @Override
    public ValueGetter getValueGetter(Attribute attribute, Engine engine,
                                      GUIFramework framework, Closeable model) {
        return new ValueGetter() {

            @Override
            public Object getValue(TableNode node, int index) {
                return "[" + ResourceLoader.getString("stream") + "]";
            }
        };
    }

}

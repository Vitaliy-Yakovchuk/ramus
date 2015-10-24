package com.ramussoft.report.editor.xml;

import java.awt.Graphics2D;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import com.ramussoft.report.editor.xml.components.Report;
import com.ramussoft.report.editor.xml.components.Table;
import com.ramussoft.report.editor.xml.components.XMLComponent;
import com.ramussoft.reportgef.Group;
import com.ramussoft.reportgef.gui.GEFComponent;
import com.ramussoft.reportgef.model.Bounds;

public class ReportEditor extends GEFComponent {

    private XMLDiagram xmlDiagram;

    private AddLabalAction addLabalAction = new AddLabalAction();

    private AddTableAction addTableAction = new AddTableAction();

    private RemoveComponentAction removeComponentAction = new RemoveComponentAction();

    private AddTableColumnAction addTableColumnAction = new AddTableColumnAction();

    private ExportToXML exportToXML = new ExportToXML();

    private ImportFromXML importFromXML = new ImportFromXML();

    private Report report = new Report();

    public ReportEditor(XMLDiagram xmlDiagram) {
        super(xmlDiagram);
        this.xmlDiagram = xmlDiagram;
        xmlDiagram.setEditor(this);
    }

    /**
     *
     */
    private static final long serialVersionUID = -7664569411500050496L;

    public Action[] getActions() {
        return new Action[]{addLabalAction, addTableAction,
                addTableColumnAction, removeComponentAction, importFromXML,
                exportToXML};
    }

    private class AddLabalAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -4782229468121799070L;

        public AddLabalAction() {
            putValue(ACTION_COMMAND_KEY, "Action.AddLabel");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/report/label.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            createNewLabal();
            postChanged();
        }

    }

    private class AddTableAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -4782229468121799070L;

        public AddTableAction() {
            putValue(ACTION_COMMAND_KEY, "Action.AddTable");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/report/table.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            createNewTable();
            postChanged();
        }

    }

    private class AddTableColumnAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -4782229468121799070L;

        public AddTableColumnAction() {
            putValue(ACTION_COMMAND_KEY, "Action.AddTableColumn");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/report/table-insert-column.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            createNewTableColumn();
            postChanged();
        }

    }

    private class RemoveComponentAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = -809829466281350412L;

        public RemoveComponentAction() {
            putValue(ACTION_COMMAND_KEY,
                    "Action.RemoveReportSelectedComponents");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_DELETE, 0));
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/delete.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            xmlDiagram.removeSelectedComponents();
            repaint();
            postChanged();
        }
    }

    ;

    public void createNewLabal() {
        xmlDiagram.createNewBounds("Label");
        repaint();
    }

    ;

    public void postChanged() {
        setActionsEnability();
    }

    public void createNewTableColumn() {
        xmlDiagram.createTableColumn();
        setActionsEnability();
        repaint();
    }

    public void createNewTable() {
        xmlDiagram.createNewBounds("Table");
        repaint();
    }

    ;

    @Override
    protected Group createGroup() {
        return new Group() {
            /**
             *
             */
            private static final long serialVersionUID = -8143041041002644334L;

            @Override
            public void applyTransforms() {
                xmlDiagram.applyTransformForGroup(this);
                clear();
            }
        };
    }

    @Override
    protected void selectionChanged() {
        setActionsEnability();
    }

    private void setActionsEnability() {
        Bounds[] bounds = getSelection().getBounds();
        if (bounds.length > 0) {
            if ((xmlDiagram.getComponent(bounds[0]) instanceof Table)) {
                Table table = (Table) xmlDiagram.getComponent(bounds[0]);
                addTableColumnAction.setEnabled(table.getColumns().length < 20);
            } else
                addTableColumnAction.setEnabled(false);

            removeComponentAction.setEnabled(true);
        }
    }

    @Override
    protected void paintSelection(Graphics2D g) {
    }

    public void saveToStream(OutputStream stream) throws IOException {
        ReportSaveXMLReader reader = new ReportSaveXMLReader(xmlDiagram, report);
        Transformer t;
        try {
            t = TransformerFactory.newInstance().newTransformer();
            SAXSource xmlSource = new SAXSource(reader, null);
            t.transform(xmlSource, new StreamResult(stream));
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Report getReport() {
        return report;
    }

    public void loadFromYComponents(List<XMLComponent> yComponents) {
        xmlDiagram.loadFromYComponents(yComponents);
        clearSelection();
    }

    public void clear() {
        xmlDiagram.clear();
    }

    public void checkSelection() {
        for (Bounds bounds : getSelection().getBounds()) {
            if (xmlDiagram.getIndexOfBounds(bounds) < 0) {
                clearSelection();
                return;
            }
        }
        selectionChanged();
    }

    private class ExportToXML extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 1428852811300811953L;

        public ExportToXML() {
            putValue(ACTION_COMMAND_KEY, "Action.ExportReportToXML");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/export.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exportToXML();
        }

    }

    ;

    private class ImportFromXML extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 5690589071398243015L;

        public ImportFromXML() {
            putValue(ACTION_COMMAND_KEY, "Action.ImportReportFromXML");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/import.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            importFromXML();
        }

    }

    public void importFromXML() {
    }

    public void exportToXML() {
    }

    ;
}

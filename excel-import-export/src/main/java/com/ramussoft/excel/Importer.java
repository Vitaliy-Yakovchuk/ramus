package com.ramussoft.excel;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;

public class Importer {

    private GUIFramework framework;

    private RowSet rowSet;

    private ArrayList<String> header = new ArrayList<String>();

    private ArrayList<JComboBox>[] boxes;

    private int sheetNumber;

    private ExcelPlugin plugin;

    private int startFrom = 1;

    private ArrayList<ImportRule> importRules = new ArrayList<ImportRule>();

    private JCheckBox applyToAll;

    private JCheckBox uniqueElement;

    public Importer(GUIFramework framework, RowSet rowSet, ExcelPlugin plugin) {
        this.framework = framework;
        this.rowSet = rowSet;
        this.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
    public void importFromFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        final Workbook workbook = new HSSFWorkbook(fileInputStream);
        fileInputStream.close();
        int sheetCount = workbook.getNumberOfSheets();
        if (sheetCount == 0) {
            JOptionPane.showMessageDialog(framework.getMainFrame(), plugin
                    .getString("NoSheetsAreFound"));
        }

        boxes = new ArrayList[sheetCount];
        for (int i = 0; i < sheetCount; i++)
            boxes[i] = new ArrayList<JComboBox>();

        final JTabbedPane pane = new JTabbedPane();

        for (int i = 0; i < sheetCount; i++) {
            sheetNumber = i;
            Sheet sheet = workbook.getSheetAt(i);
            pane.addTab(workbook.getSheetName(i), createSheetSelect(sheet));
        }

        BaseDialog dialog = new BaseDialog(framework.getMainFrame(), true) {

            /**
             *
             */
            private static final long serialVersionUID = -5962006392465638821L;

            @Override
            protected void onOk() {
                rowSet.startUserTransaction();
                try {
                    int index = pane.getSelectedIndex();
                    ArrayList<JComboBox> boxes = Importer.this.boxes[index];
                    Sheet sheet = workbook.getSheetAt(index);

                    ArrayList<ImportRule> rules = new ArrayList<ImportRule>();
                    int attr = 0;
                    for (JComboBox box : boxes) {
                        ImportRule source = importRules.get(attr);

                        int column = box.getSelectedIndex() - 2;
                        if (column >= -1) {
                            ImportRule rule = new ImportRule(source
                                    .getAttribute(),
                                    source.getTableAttribute(), column);
                            rules.add(rule);
                        }
                        attr++;
                    }
                    if (applyToAll.isSelected()) {
                        for (index = 0; index < workbook.getNumberOfSheets(); index++) {
                            try {
                                sheet = workbook.getSheetAt(index);
                                ComplexImport import1 = new ComplexImport(
                                        rowSet, uniqueElement.isSelected());
                                import1.importDromSheet(sheet, workbook
                                        .getSheetName(index), startFrom, rules
                                        .toArray(new ImportRule[rules.size()]));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        ComplexImport import1 = new ComplexImport(rowSet,
                                uniqueElement.isSelected());
                        import1.importDromSheet(sheet, workbook
                                .getSheetName(index), startFrom, rules
                                .toArray(new ImportRule[rules.size()]));
                    }

                } finally {
                    rowSet.commitUserTransaction();
                }
                super.onOk();
            }
        };

        dialog.setTitle(plugin.getString("Action.ImportFromExcel"));

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(pane, BorderLayout.CENTER);

        applyToAll = new JCheckBox(plugin.getString("ApplyToAllSheets"));

        uniqueElement = new JCheckBox(plugin.getString("UniqueElements"));

        uniqueElement.setSelected(true);

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel2.add(applyToAll);
        panel2.add(uniqueElement);

        panel.add(panel2, BorderLayout.SOUTH);

        dialog.setMainPane(panel);
        dialog.pack();
        dialog.setMaximumSize(dialog.getSize());
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private JPanel createSheetSelect(Sheet sheet) {
        double[] x = {5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5};
        Qualifier qualifier = rowSet.getQualifier();
        int boxCount = 0;

        for (Attribute attribute : qualifier.getAttributes()) {
            if (attribute.getAttributeType().toString().equals("Core.Table")) {
                boxCount += StandardAttributesPlugin
                        .getTableQualifierForAttribute(rowSet.getEngine(),
                                attribute).getAttributes().size();
            } else {
                boxCount++;
            }
        }

        double[] y = new double[boxCount * 2 + 1];

        y[0] = 5;
        for (int i = 0; i < boxCount; i++) {
            y[i * 2 + 1] = TableLayout.MINIMUM;
            y[i * 2 + 2] = 5;
        }

        double[][] size = {x, y};
        TableLayout layout = new TableLayout(size);

        JPanel panel = new JPanel(layout);

        Row row = sheet.getRow(0);
        int count = 0;
        header.clear();
        if (row != null)
            while (row.getCell(count) != null) {
                String value = row.getCell(count).getStringCellValue();
                if ((value == null) || (value.equals(""))) {
                    startFrom = 2;
                }
                header.add(value);
                count++;
            }

        if (startFrom > 1) {
            Row hr = sheet.getRow(1);
            if (hr != null)
                for (int c = 0; c < count; c++) {
                    Cell cell = hr.getCell(c);
                    if (cell != null) {
                        String tmp = cell.getStringCellValue();
                        if ((tmp != null) && (!"".equals(tmp))) {
                            header.set(c, header.get(c) + " " + tmp);
                        }
                    }
                }
        }

        int i = 1;

        for (Attribute attribute : qualifier.getAttributes()) {
            if (attribute.getAttributeType().toString().equals("Core.Table")) {
                for (Attribute tableAttribute : StandardAttributesPlugin
                        .getTableQualifierForAttribute(rowSet.getEngine(),
                                attribute).getAttributes()) {
                    JLabel label = new JLabel(attribute.getName() + "."
                            + tableAttribute.getName());
                    panel.add(label, "1," + i);
                    panel.add(createCombo(), "3," + i);
                    i += 2;
                    importRules.add(new ImportRule(attribute, tableAttribute,
                            -1));
                }
            } else {
                JLabel label = new JLabel(attribute.getName());

                panel.add(label, "1," + i);
                panel.add(createCombo(), "3," + i);
                i += 2;
                importRules.add(new ImportRule(attribute, null, -1));
            }
        }

        return panel;
    }

    private JComboBox createCombo() {
        JComboBox box = new JComboBox();
        box.addItem(plugin.getString("DoNotImport"));
        box.addItem(plugin.getString("SheetName"));
        for (String s : header) {
            box.addItem(s);
        }
        boxes[sheetNumber].add(box);
        return box;
    }

}

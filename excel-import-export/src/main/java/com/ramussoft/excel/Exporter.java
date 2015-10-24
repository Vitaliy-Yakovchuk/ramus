package com.ramussoft.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.qualifier.table.RowTreeTableModel;
import com.ramussoft.gui.qualifier.table.TableView;
import com.ramussoft.gui.qualifier.table.TreeTableNode;

public class Exporter {

    private final TableView tableView;

    public Exporter(TableView tableView) {
        this.tableView = tableView;
    }

    public void exportToFile(List<Attribute> attributes, Qualifier qualifier,
                             File file) throws IOException {
        Workbook workbook = new HSSFWorkbook();
        CellStyle headerStyle;
        Font headerFont = workbook.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerStyle = createBorderedStyle(workbook);
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE
                .getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerStyle.setFont(headerFont);

        CellStyle normalStyle;

        normalStyle = createBorderedStyle(workbook);
        normalStyle.setAlignment(CellStyle.ALIGN_LEFT);
        normalStyle.setWrapText(true);

        CellStyle dateStyle = createDateStyle(workbook);

        String name = qualifier.getName();
        Sheet sheet;
        if ((name == null) || (name.equals("")))
            sheet = workbook.createSheet();
        else
            sheet = workbook.createSheet(name);
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        sheet.setAutobreaks(true);

        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(12.75f);
        for (int i = 0; i < attributes.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(attributes.get(i).getName());
            cell.setCellStyle(headerStyle);
        }

        RowSet rowSet = tableView.getComponent().getRowSet();
        RowTreeTableModel model = tableView.getComponent().getModel();
        int i = 1;
        for (com.ramussoft.database.common.Row row : rowSet.getAllRows()) {
            Row cellRow = sheet.createRow(i);
            int j = 0;
            for (Attribute attribute : attributes) {
                if (attribute.getAttributeType().isLight()) {
                    Cell cell = cellRow.createCell(j);
                    cell.setCellStyle(normalStyle);
                    Object object = row.getAttribute(attribute);
                    if (object != null) {
                        if (object instanceof Date) {
                            cell.setCellStyle(dateStyle);
                            cell.setCellValue((Date) object);
                        } else {
                            TreeTableNode node = model.findNode(row);
                            if (node != null) {
                                int index = rowSet.getAttributeIndex(attribute) - 1;
                                if (index >= 00) {
                                    Object object2 = model.getValueAt(node,
                                            index);
                                    if (object2 != null)
                                        cell.setCellValue(object2.toString());
                                }
                            }
                        }

                    }
                }

                j++;
            }
            i++;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat()
                .getFormat("m/d/yy"));

        return style;
    }

    private static CellStyle createBorderedStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }

}

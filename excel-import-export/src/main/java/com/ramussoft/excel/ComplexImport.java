package com.ramussoft.excel;

import java.util.Hashtable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.OtherElementPropertyPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.eval.EObject;

public class ComplexImport {

    private RowSet rowSet;

    private String sheetName;

    private Hashtable<Attribute, Long> otherElemetsQualifierId = new Hashtable<Attribute, Long>();

    private boolean uniqueName;

    public ComplexImport(RowSet rowSet, boolean uniqueName) {
        this.rowSet = rowSet;
        this.uniqueName = uniqueName;
    }

    public void importDromSheet(Sheet sheet, String sheetName, int startFrom,
                                ImportRule[] rules) {

        this.sheetName = sheetName;

        Attribute name = rowSet.getEngine().getAttribute(
                rowSet.getQualifier().getAttributeForName());

        Row current = null;

        int row = startFrom;
        while (true) {
            org.apache.poi.ss.usermodel.Row row2 = sheet.getRow(row);
            if (row2 == null)
                break;
            for (ImportRule rule : rules) {
                if (rule.getAttribute().equals(name)) {
                    String s = null;
                    if (rule.getColumn() >= 0) {
                        Cell cell = row2.getCell(rule.getColumn());
                        if (cell != null) {
                            s = cell.getStringCellValue();
                        }
                    } else
                        s = sheetName;

                    if ((s != null) && (!"".equals(s))) {
                        Element element = null;

                        if (uniqueName)
                            element = rowSet.getEngine().getElement(s,
                                    rowSet.getQualifier().getId());
                        if (element != null)
                            current = null;
                        else {
                            current = rowSet.createRow(null);
                        }
                        for (ImportRule rule2 : rules)
                            rule2.setObject(null);
                    }
                }
            }

            Hashtable<Attribute, Element> tableElements = new Hashtable<Attribute, Element>(
                    1);

            if (current != null)
                for (ImportRule rule : rules) {
                    if (rule.getColumn() == -1) {
                        if (rule.getTableAttribute() == null) {
                            fill(current.getElement(), null, rule
                                    .getAttribute(), rule);
                        } else {
                            Element tableElement = tableElements.get(rule
                                    .getAttribute());
                            if (tableElement == null) {
                                tableElement = StandardAttributesPlugin
                                        .createTableElement(rowSet.getEngine(),
                                                rule.getAttribute(), current
                                                        .getElement());
                                tableElements.put(rule.getAttribute(),
                                        tableElement);
                            }
                            fill(tableElement, null, rule.getTableAttribute(),
                                    rule);
                        }
                    } else {
                        Cell cell = row2.getCell(rule.getColumn());
                        if (cell != null) {
                            if (rule.getTableAttribute() == null) {
                                fill(current.getElement(), cell, rule
                                        .getAttribute(), rule);
                            } else {
                                if (!isNull(cell)) {
                                    Element tableElement = tableElements
                                            .get(rule.getAttribute());
                                    if (tableElement == null) {
                                        tableElement = StandardAttributesPlugin
                                                .createTableElement(rowSet
                                                                .getEngine(), rule
                                                                .getAttribute(),
                                                        current.getElement());
                                        tableElements.put(rule.getAttribute(),
                                                tableElement);
                                    }
                                    fill(tableElement, cell, rule
                                            .getTableAttribute(), rule);
                                }
                            }
                        }
                    }
                }

            row++;
        }
    }

    private boolean isNull(Cell cell) {

        try {
            String string = cell.getStringCellValue();
            if ((string == null) || (string.length() == 0))
                return true;
        } catch (Exception e) {

        }
        try {
            if (cell.getDateCellValue() == null)
                return true;
        } catch (Exception e) {

        }
        try {
            if (cell.getRichStringCellValue() == null)
                return true;
        } catch (Exception e) {

        }
        return false;
    }

    private void fill(Element element, Cell cell, Attribute attribute,
                      ImportRule rule) {
        Engine engine = rowSet.getEngine();
        String type = attribute.getAttributeType().toString();

        if (cell == null) {
            try {
                engine.setAttribute(element, attribute, sheetName);
            } catch (Exception e) {

            }

        } else {
            try {
                EObject object = null;
                if (cell.getCellType() == Cell.CELL_TYPE_STRING)
                    object = new EObject(cell.getStringCellValue());
                else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                    object = new EObject(cell.getNumericCellValue());
                else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                    object = new EObject(cell.getDateCellValue());
                else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
                    object = new EObject(cell.getDateCellValue());
                if (object == null)
                    try {
                        object = new EObject(cell.getStringCellValue());
                    } catch (Exception e) {
                    }
                if (object == null)
                    try {
                        object = new EObject(cell.getNumericCellValue());
                    } catch (Exception e) {
                    }
                if (object == null)
                    try {
                        object = new EObject(cell.getDateCellValue());
                    } catch (Exception e) {
                    }

                if (object == null)
                    return;
                if (("".equals(object.getValue()))
                        && (engine.getAttribute(element, attribute) != null)) {
                    setAttribute(engine, element, attribute, null, rule);
                    return;
                }
                if ((object.getValue() == null)
                        && (engine.getAttribute(element, attribute) != null)) {
                    setAttribute(engine, element, attribute, null, rule);
                    return;
                }

                if (("Core.Double".equals(type))
                        || ("Core.Currency".equals(type))) {
                    setAttribute(engine, element, attribute, object
                            .doubleValue(), rule);
                } else if (("Core.Text".equals(type))
                        || ("Core.Variant".equals(type))) {
                    setAttribute(engine, element, attribute, object
                            .stringValue(), rule);
                } else if ("Core.Date".equals(type)) {
                    setAttribute(engine, element, attribute,
                            object.dateValue(), rule);
                } else if ("Core.Long".equals(type)) {
                    setAttribute(engine, element, attribute,
                            object.longValue(), rule);

                } else if ("Core.OtherElement".equals(type)) {
                    long qId = getOtherElemetQualifierId(attribute);
                    String s = object.stringValue();
                    if ((s != null) && (s.length() > 0)) {
                        Element element2 = engine.getElement(s.trim(), qId);
                        if (element2 == null) {
                            Qualifier qualifier = engine.getQualifier(qId);
                            Attribute name = engine.getAttribute(qualifier
                                    .getAttributeForName());
                            if (name == null)
                                return;
                            element2 = engine.createElement(qId);
                            setAttribute(engine, element2, name, s, rule);

                        }
                        setAttribute(engine, element, attribute, element2
                                .getId(), rule);
                    }
                } else {
                    setAttribute(engine, element, attribute, object.getValue(),
                            rule);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setAttribute(Engine engine, Element element,
                              Attribute attribute, Object object, ImportRule rule) {
        if ((object == null)
                || ((object instanceof String) && (object.toString().length() == 0))) {
            object = rule.getObject();
        } else
            rule.setObject(object);
        engine.setAttribute(element, attribute, object);
    }

    public long getOtherElemetQualifierId(Attribute attribute) {
        Long long1 = otherElemetsQualifierId.get(attribute);
        if (long1 == null) {
            OtherElementPropertyPersistent pp = (OtherElementPropertyPersistent) rowSet
                    .getEngine().getAttribute(null, attribute);
            long1 = pp.getQualifier();
            otherElemetsQualifierId.put(attribute, long1);
        }
        return long1;
    }

}

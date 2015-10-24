package com.ramussoft.gui.common;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public interface AttributePlugin extends GUIPlugin {

    AttributeEditor getAttributeEditor(Engine engine, AccessRules rules,
                                       Element element, Attribute attribute, String propertiesPrefix, AttributeEditor oldAttributeEditor);

    TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                       Attribute attribute);

    TableCellRenderer getTableCellRenderer(Engine engine, AccessRules rules,
                                           Attribute attribute);

    AttributeType getAttributeType();

    AttributePreferenciesEditor getAttributePreferenciesEditor();

    boolean isCellEditable();

    Attribute createSyncAttribute(Engine engine, QualifierImporter importer,
                                  Attribute sourceAttribute);

    void syncAttribute(Engine engine, QualifierImporter importer,
                       Attribute sourceAttribute);

    void syncElement(Engine engine, QualifierImporter importer,
                     Element sourceElement, Attribute sourceAttribute);

    int getSyncPriority();

}

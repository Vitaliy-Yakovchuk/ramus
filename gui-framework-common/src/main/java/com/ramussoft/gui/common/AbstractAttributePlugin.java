package com.ramussoft.gui.common;

import java.awt.*;
import java.net.URI;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import edu.stanford.ejalbert.BrowserLauncher;

public abstract class AbstractAttributePlugin implements AttributePlugin {

    protected GUIFramework framework;

    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, Element element,
                                              final Attribute attribute, AttributeEditor oldAttributeEditor) {
        if (isCellEditable()) {
            if (oldAttributeEditor != null)
                oldAttributeEditor.close();
            return new AttributeCellEditorToAttributeEditor(getTableCellEditor(
                    engine, rules, attribute));
        }
        return null;
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, Element element,
                                              final Attribute attribute, String propertiesPrefix,
                                              AttributeEditor oldAttributeEditor) {
        return getAttributeEditor(engine, rules, element, attribute,
                oldAttributeEditor);
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return new DefaultTableCellRenderer();
    }

    @Override
    public void setFramework(GUIFramework framework) {
        this.framework = framework;
    }

    @Override
    public String getString(String key) {
        return GlobalResourcesManager.getString(key);
    }

    @Override
    public AttributePreferenciesEditor getAttributePreferenciesEditor() {
        return null;
    }

    @Override
    public boolean isCellEditable() {
        return true;
    }

    @Override
    public Attribute createSyncAttribute(Engine engine,
                                         QualifierImporter importer, Attribute sourceAttribute) {

        Attribute result = findAtribute(engine, sourceAttribute);
        if (result == null) {
            result = engine.createAttribute(sourceAttribute.getAttributeType());
            result.setName(sourceAttribute.getName());
            engine.updateAttribute(result);
        }
        return result;
    }

    protected Attribute findAtribute(Engine engine, Attribute sourceAttribute) {
        Attribute result = null;
        List<Attribute> attributes = engine.getAttributes();
        for (Attribute attr : attributes) {
            if (isSameAttribute(sourceAttribute, attr)) {
                result = attr;
                break;
            }
        }
        return result;
    }

    protected boolean isSameAttribute(Attribute sourceAttribute,
                                      Attribute destAttribute) {
        return (destAttribute.getName().equals(sourceAttribute.getName()))
                && (destAttribute.getAttributeType().equals(sourceAttribute
                .getAttributeType()));
    }

    @Override
    public void syncElement(Engine engine, QualifierImporter importer,
                            Element sourceElement, Attribute sourceAttribute) {
        Element dest = importer.getDestination(sourceElement);
        engine.setAttribute(dest, importer.getDestination(sourceAttribute),
                importer.getSourceValue(sourceElement, sourceAttribute));
    }

    @Override
    public void syncAttribute(Engine engine, QualifierImporter importer,
                              Attribute sourceAttribute) {
    }

    @Override
    public int getSyncPriority() {
        return 1;
    }


    public static boolean openUrl(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean openUrl(String url) {
        return openUrl(url, framework);
    }

    public static boolean openUrl(String url, GUIFramework framework) {
        try {
            return openUrl(new URI(url));
        } catch (Exception e) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e1) {
                try {
                    new BrowserLauncher().openURLinBrowser(url);
                } catch (Exception e2) {
                    e1.printStackTrace();
                    e2.printStackTrace();
                    JOptionPane.showMessageDialog(framework.getMainFrame(),
                            e1.getLocalizedMessage());
                }
            }
        }
        return false;
    }
}

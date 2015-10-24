package com.ramussoft.report.editor.xml;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.report.editor.xml.components.Label;
import com.ramussoft.report.editor.xml.components.Table;
import com.ramussoft.report.editor.xml.components.TableColumn;
import com.ramussoft.reportgef.AbstractComponentFactory;
import com.ramussoft.reportgef.Component;
import com.ramussoft.reportgef.ComponentFactory;
import com.ramussoft.reportgef.GEFFramework;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Bounds;

public class XMLComponentFramefork extends GEFFramework {

    public XMLComponentFramefork() {
        super(null);
        componentFactories.put("Label", createLabalComponentFactory());
        componentFactories.put("Table", createTableComponentFactory());
        componentFactories.put("TableColumn", createTableColumnFactory());

    }

    private ComponentFactory createTableColumnFactory() {
        return new AbstractComponentFactory() {

            @Override
            public String getType() {
                return "TableColumn";
            }

            @Override
            public Component createComponent(Diagram diagram, Engine engine,
                                             AccessRules accessRules, Bounds bounds) {
                TableColumn label = new TableColumn();
                label.setWidth(((XMLDiagram) diagram).getWidthForCompontns());
                return label;
            }

            @Override
            public Component getComponent(Engine engine,
                                          AccessRules accessRules, Bounds bounds) {
                return null;
            }
        };
    }

    private ComponentFactory createTableComponentFactory() {
        return new AbstractComponentFactory() {

            @Override
            public String getType() {
                return "Table";
            }

            @Override
            public Component createComponent(Diagram diagram, Engine engine,
                                             AccessRules accessRules, Bounds bounds) {
                Table label = new Table();
                label.setWidth(((XMLDiagram) diagram).getWidthForCompontns());
                return label;
            }

            @Override
            public Component getComponent(Engine engine,
                                          AccessRules accessRules, Bounds bounds) {
                return null;
            }
        };
    }

    private ComponentFactory createLabalComponentFactory() {
        return new AbstractComponentFactory() {

            @Override
            public String getType() {
                return "Label";
            }

            @Override
            public Component createComponent(Diagram diagram, Engine engine,
                                             AccessRules accessRules, Bounds bounds) {
                Label label = new Label();
                label.setWidth(((XMLDiagram) diagram).getWidthForCompontns());
                return label;
            }

            @Override
            public Component getComponent(Engine engine,
                                          AccessRules accessRules, Bounds bounds) {
                return null;
            }
        };
    }

}

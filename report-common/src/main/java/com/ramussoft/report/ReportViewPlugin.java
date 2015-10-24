package com.ramussoft.report;

import jsyntaxpane.DefaultSyntaxKit;

import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.TabbedEvent;
import com.ramussoft.gui.common.event.ViewTitleEvent;
import com.ramussoft.gui.qualifier.table.TabbedTableView;
import com.ramussoft.report.editor.DocBookScriptReportEditorView;
import com.ramussoft.report.editor.ReportEditorView;
import com.ramussoft.report.editor.ScriptReportEditorView;
import com.ramussoft.report.editor.XMLReportEditorView;

public class ReportViewPlugin extends AbstractViewPlugin {

    protected static final String REPORTS_TAB_VIEW = TabbedTableView.MAIN_TABBED_VIEW;

    public static final String OPEN_SCRIPT_REPORT = "OpenScriptReport";

    static {
        DefaultSyntaxKit.initKit();
    }

    private Engine engine;

    @Override
    public String getName() {
        return "ReportOrganization";
    }

    @Override
    public void setFramework(GUIFramework framework) {
        super.setFramework(framework);
        this.engine = framework.getEngine();
        addOpenReportEditorListener();
        framework.setSystemAttributeName(
                ReportPlugin.getReportNameAttribute(engine),
                GlobalResourcesManager.getString("AttributeName"));
    }

    @Override
    public UniqueView[] getUniqueViews() {
        return new UniqueView[]{new ReportsView(framework),
                new ReportAttributesView(framework)};
    }

    public String getString(String key) {
        return ReportResourceManager.getString(key);
    }

    private void addOpenReportEditorListener() {
        framework.addActionListener(OPEN_SCRIPT_REPORT, new ActionListener() {

            @Override
            public void onAction(final ActionEvent event) {

                if (framework.openView(event))
                    return;

                final Data data = new Data();

                data.element = (Element) event.getValue();

                data.attributeListener = new ElementAttributeListener() {
                    @Override
                    public void attributeChanged(AttributeEvent event) {
                        Object value = event.getNewValue();

                        if ((value instanceof String)
                                && (event.getElement().equals(data.element))) {
                            ViewTitleEvent e = new ViewTitleEvent(data.view,
                                    value.toString());
                            data.view.titleChanged(e);
                        }
                    }
                };

                data.elementListener = new ElementAdapter() {

                    @Override
                    public void elementDeleted(ElementEvent event) {
                        if (event.getOldElement().equals(data.element)) {
                            data.view.close();
                        }
                    }

                };

                engine.addElementListener(
                        ReportPlugin.getReportsQualifier(engine),
                        data.elementListener);
                engine.addElementAttributeListener(
                        ReportPlugin.getReportsQualifier(engine),
                        data.attributeListener);

                String type = (String) engine.getAttribute(data.element,
                        ReportPlugin.getReportTypeAttribute(engine));

                if (ReportPlugin.TYPE_JSSP.equals(type))

                    data.view = new ScriptReportEditorView(framework,
                            data.element) {

                        @Override
                        public void close() {
                            super.close();
                            engine.removeElementListener(
                                    ReportPlugin.getReportsQualifier(engine),
                                    data.elementListener);
                            engine.removeElementAttributeListener(
                                    ReportPlugin.getReportsQualifier(engine),
                                    data.attributeListener);

                            TabbedEvent tEvent = new TabbedEvent(
                                    "TabbedTableView", this);
                            tabRemoved(tEvent);
                        }

                        @Override
                        public String getTitle() {
                            return data.element.getName();
                        }

                    };
                else if (ReportPlugin.TYPE_JSSP_DOC_BOOK.equals(type))

                    data.view = new DocBookScriptReportEditorView(framework,
                            data.element) {

                        @Override
                        public void close() {
                            super.close();
                            engine.removeElementListener(
                                    ReportPlugin.getReportsQualifier(engine),
                                    data.elementListener);
                            engine.removeElementAttributeListener(
                                    ReportPlugin.getReportsQualifier(engine),
                                    data.attributeListener);

                            TabbedEvent tEvent = new TabbedEvent(
                                    "TabbedTableView", this);
                            tabRemoved(tEvent);
                        }

                        @Override
                        public String getTitle() {
                            return data.element.getName();
                        }

                    };
                else

                    data.view = new XMLReportEditorView(framework, data.element) {

                        @Override
                        public void close() {
                            super.close();
                            engine.removeElementListener(
                                    ReportPlugin.getReportsQualifier(engine),
                                    data.elementListener);
                            engine.removeElementAttributeListener(
                                    ReportPlugin.getReportsQualifier(engine),
                                    data.attributeListener);

                            TabbedEvent tEvent = new TabbedEvent(
                                    "TabbedTableView", this);
                            tabRemoved(tEvent);
                        }

                        @Override
                        public String getTitle() {
                            return data.element.getName();
                        }

                    };

                TabbedEvent tEvent = new TabbedEvent(REPORTS_TAB_VIEW,
                        data.view);
                tabCreated(tEvent);
            }

        });
    }

    private class Data {

        protected ReportEditorView view;
        protected ElementAdapter elementListener;
        protected ElementAttributeListener attributeListener;
        protected Element element;

    }

    ;

}

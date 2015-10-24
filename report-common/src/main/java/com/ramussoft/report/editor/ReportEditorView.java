package com.ramussoft.report.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import com.ramussoft.common.Element;
import com.ramussoft.gui.StandardFilePlugin;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.print.HTMLPrintable;
import com.ramussoft.report.Query;
import com.ramussoft.report.ReportQuery;
import com.ramussoft.report.ReportResourceManager;
import com.ramussoft.report.ReportViewPlugin;
import com.ramussoft.report.data.DataException;
import com.ramussoft.report.data.MessageFormatter;

public class ReportEditorView extends AbstractView implements TabView {

    protected QueryView queryView;

    protected PreviewView previewView;

    protected HTMLView htmlView;

    protected JPanel content = new JPanel(new BorderLayout());

    protected SubView activeView;

    protected Element element;

    private com.ramussoft.gui.common.event.ActionListener beforeSaveAction;

    private Thread loading = null;

    protected JPanel buttonsPanel;

    private Object lock = new Object();

    public ReportEditorView(final GUIFramework framework, final Element element) {
        super(framework);
        this.element = element;
        htmlView = createHTMLView();
        queryView = new QueryView(framework);
        previewView = new PreviewView(this);
        beforeSaveAction = new com.ramussoft.gui.common.event.ActionListener() {

            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                save();
            }
        };
        framework.addActionListener("BeforeFileSave", beforeSaveAction);
    }

    protected HTMLView createHTMLView() {
        return new HTMLView(this);
    }

    @Override
    public JComponent createComponent() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(content, BorderLayout.CENTER);
        buttonsPanel = new JPanel(new GridLayout(1, 4, 5, 5));

        ButtonGroup group = new ButtonGroup();

        createButtons(group);

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel2.add(buttonsPanel);
        panel.add(panel2, BorderLayout.SOUTH);

        return panel;
    }

    protected void createButtons(ButtonGroup group) {
        buttonsPanel.add(createOpenViewButton(group, htmlView));
        buttonsPanel.add(createOpenViewButton(group, previewView));
    }

    protected JToggleButton createOpenViewButton(ButtonGroup group,
                                                 final SubView view) {
        JToggleButton button = new JToggleButton(view.getTitle());
        group.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                actionsRemoved(activeView.getActions());
                beforeSubviewActivated(view);
                content.removeAll();
                content.add(view, BorderLayout.CENTER);
                content.revalidate();
                content.repaint();
                activeView = view;
                actionsAdded(view.getActions());
            }
        });

        return button;
    }

    @SuppressWarnings("deprecation")
    public void beforeSubviewActivated(final SubView view) {
        if ((view == htmlView) || (view == previewView)) {

            synchronized (lock) {
                if (loading != null)
                    loading.stop();
                loading = null;
                setText(view, ReportResourceManager.getString("Report.Loading"));
            }

            synchronized (lock) {
                loading = new Thread("Report.Loading") {
                    @Override
                    public void run() {
                        final String page = getHTMLText();
                        synchronized (lock) {
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    synchronized (lock) {
                                        setText(view, page);
                                    }
                                }
                            });
                            loading = null;
                        }
                    }
                };
                loading.start();
            }


        } else if (view == queryView) {
            queryView.setQueryForReport(element);
        }
    }

    private void setText(SubView view, String page) {
        if (view == htmlView)
            htmlView.setHTMLText(page);

        else if (view == previewView)
            previewView.setHTMLText(page);
    }

    protected String getHTMLText() {
        String page;
        try {
            HashMap<String, Object> map = new HashMap<String, Object>();
            Query query = queryView.getQuery();
            if (query != null)
                map.put("query", query);
            page = ((ReportQuery) framework.getEngine()).getHTMLReport(element,
                    map);
        } catch (Exception e1) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            PrintStream s = new PrintStream(stream);
            e1.printStackTrace();
            if (e1 instanceof DataException)
                s.println(((DataException) e1)
                        .getMessage(new MessageFormatter() {

                            @Override
                            public String getString(String key,
                                                    Object[] arguments) {
                                return MessageFormat.format(
                                        ReportResourceManager.getString(key),
                                        arguments);
                            }
                        }));
            else {
                e1.printStackTrace(s);
            }

            s.flush();

            page = new String(stream.toByteArray());
        }
        return page;
    }

    @Override
    public Action[] getActions() {
        return activeView.getActions();
    }

    @Override
    public String getString(String key) {
        try {
            return ReportResourceManager.getString(key);
        } catch (Exception e) {
            return super.getString(key);
        }
    }

    @Override
    public String getTitle() {
        return element.getName();
    }

    @Override
    public void close() {
        super.close();
        framework.removeActionListener("BeforeFileSave", beforeSaveAction);
        if (queryView != null)
            queryView.close();
    }

    protected void save() {
    }

    @Override
    public String[] getGlobalActions() {
        return new String[]{StandardFilePlugin.ACTION_PRINT,
                StandardFilePlugin.ACTION_PAGE_SETUP,
                StandardFilePlugin.ACTION_PRINT_PREVIEW};
    }

    @Override
    public void onAction(com.ramussoft.gui.common.event.ActionEvent event) {
        if (event.getKey().equals(StandardFilePlugin.ACTION_PAGE_SETUP))
            new HTMLPrintable().pageSetup(framework);
        else if (event.getKey().equals(StandardFilePlugin.ACTION_PRINT)) {
            HTMLPrintable printable = new HTMLPrintable();
            init(printable);
            try {
                printable.print(framework);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(framework.getMainFrame(), e
                        .getLocalizedMessage());
                e.printStackTrace();
            }
        } else if (event.getKey().equals(
                StandardFilePlugin.ACTION_PRINT_PREVIEW)) {
            HTMLPrintable printable = new HTMLPrintable();
            init(printable);
            framework.printPreview(printable);
        }
    }

    protected void init(HTMLPrintable printable) {
        try {

            OutputStream os = printable.getOutputStream();
            os.write(getHTMLText().getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public com.ramussoft.gui.common.event.ActionEvent getOpenAction() {
        return new com.ramussoft.gui.common.event.ActionEvent(
                ReportViewPlugin.OPEN_SCRIPT_REPORT, element);
    }
}

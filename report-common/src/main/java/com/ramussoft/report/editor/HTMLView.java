package com.ramussoft.report.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.SourceFormatter;

import com.ramussoft.report.ReportResourceManager;

public class HTMLView extends SubView {

    /**
     *
     */
    private static final long serialVersionUID = -6755265040080356266L;

    private JEditorPane editorPane = new JEditorPane();

    protected String text;

    private boolean formatt = true;

    private FormatAction formatAction = new FormatAction();

    private ReportEditorView reportEditorView;

    private Action refreshAction = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = -2250439067680196983L;

        {
            putValue(ACTION_COMMAND_KEY, "Refresh");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/refresh.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            reportEditorView.beforeSubviewActivated(HTMLView.this);
        }
    };

    public HTMLView(ReportEditorView reportEditorView) {
        this.reportEditorView = reportEditorView;
        JScrollPane pane = new JScrollPane(editorPane);
        editorPane.setContentType("text/xhtml");
        editorPane.setEditable(false);
        this.add(pane, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return ReportResourceManager.getString("HTMLView.title");
    }

    public void setHTMLText(String htmlText) {
        try {
            this.text = htmlText;
            if (formatt) {
                Source segment = new Source(text);
                segment.fullSequentialParse();
                SourceFormatter formatter = new SourceFormatter(
                        segment);
                htmlText = formatter.toString();
            }

            editorPane.read(new StringReader(htmlText), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Action[] getActions() {
        return new Action[]{refreshAction, formatAction};
    }

    private class FormatAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -1920415454476046612L;

        public FormatAction() {
            putValue(ACTION_COMMAND_KEY, "Format.HTML");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/report/format.png")));
            putValue(SELECTED_KEY, formatt);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (formatt == ((Boolean) getValue(SELECTED_KEY)))
                return;
            formatt = (Boolean) getValue(SELECTED_KEY);
            putValue(SELECTED_KEY, formatt);
            try {
                String text = HTMLView.this.text;
                if (formatt) {
                    SourceFormatter formatter = new SourceFormatter(new Source(
                            text));
                    text = formatter.toString();
                }

                editorPane.read(new StringReader(text), null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    ;
}

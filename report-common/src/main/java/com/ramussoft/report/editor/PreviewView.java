package com.ramussoft.report.editor;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.io.StringReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import com.ramussoft.report.ReportResourceManager;

public class PreviewView extends SubView {

    /**
     *
     */
    private static final long serialVersionUID = 541835544970921614L;

    private JEditorPane editorPane = new JEditorPane();

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
            reportEditorView.beforeSubviewActivated(PreviewView.this);
        }
    };

    public PreviewView(ReportEditorView reportEditorView) {
        this.reportEditorView = reportEditorView;
        JScrollPane pane = new JScrollPane(editorPane);
        HTMLEditorKit kit = new HTMLEditorKit() {
            /**
             *
             */
            private static final long serialVersionUID = -8040272164224951314L;

            @Override
            public Document createDefaultDocument() {
                Document document = super.createDefaultDocument();
                document.putProperty("IgnoreCharsetDirective", true);
                return document;
            }
        };

        editorPane.setEditorKit(kit);
        editorPane.setEditable(false);
        this.add(pane, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return ReportResourceManager.getString("PreviewView.title");
    }

    public void setHTMLText(String htmlText) {
        try {
            editorPane.read(new StringReader(htmlText), null);
        } catch (Exception e) {
            e.printStackTrace();
            editorPane.setText(e.getLocalizedMessage());
        }
    }

    @Override
    public Action[] getActions() {
        return new Action[]{refreshAction};
    }

}

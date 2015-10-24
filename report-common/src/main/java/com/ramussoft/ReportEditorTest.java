package com.ramussoft;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.ramussoft.report.editor.xml.ReportEditor;
import com.ramussoft.report.editor.xml.XMLDiagram;

public class ReportEditorTest extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1785228311441943986L;

    public ReportEditorTest() {
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = new JPanel(new BorderLayout());

        XMLDiagram diagram = new XMLDiagram();
        ReportEditor editor = new ReportEditor(diagram);

        setContentPane(panel);
        JScrollPane pane = new JScrollPane(editor);

        panel.add(pane, BorderLayout.CENTER);
        JToolBar bar = new JToolBar();
        panel.add(bar, BorderLayout.NORTH);
        for (Action action : editor.getActions()) {
            JButton button = bar.add(action);
            button.setText((String) action.getValue(Action.ACTION_COMMAND_KEY));
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new ReportEditorTest().setVisible(true);
    }

}

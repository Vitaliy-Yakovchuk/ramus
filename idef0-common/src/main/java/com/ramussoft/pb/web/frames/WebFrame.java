package com.ramussoft.pb.web.frames;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class WebFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private WebPanel webPanel = null;

    /**
     * This is the default constructor
     */
    public WebFrame() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setSize(796, 444);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(getJContentPane());
        setTitle("JFrame");
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getWebPanel(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes webPanel
     *
     * @return com.dsoft.pb.web.frames.WebPanel
     */
    private WebPanel getWebPanel() {
        if (webPanel == null) {
            webPanel = new WebPanel();
        }
        return webPanel;
    }

} // @jve:decl-index=0:visual-constraint="10,10"

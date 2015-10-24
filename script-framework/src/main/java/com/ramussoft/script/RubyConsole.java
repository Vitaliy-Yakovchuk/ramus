package com.ramussoft.script;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;

import com.ramussoft.gui.common.prefrence.Options;

public class RubyConsole extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 8110662760061913638L;
    private ScriptPlugin plugin;

    public RubyConsole(ScriptPlugin plugin) {
        this.plugin = plugin;
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        init();
    }

    private void init() {
        this.setTitle("IRBConsole");
        setContentPane(createComponent());
        Options.loadOptions(this);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Options.loadOptions(RubyConsole.this);
            }
        });
    }

    private void run(final Ruby runtime) {
        Thread t2 = new Thread() {
            public void run() {
                try {
                    InputStream is = getClass().getResourceAsStream("/console_irb.rb");

                    ByteArrayOutputStream ba = new ByteArrayOutputStream();
                    int r;
                    while ((r = is.read()) >= 0)
                        ba.write(r);
                    is.close();

                    //runtime
                    //	.evalScriptlet("require 'irb'\n require 'irb/completion'\n IRB.start(__FILE__)\n");
                    runtime.evalScriptlet(new String(ba.toByteArray()));
                    //run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t2.start();
    }

    private static Font findFont(String otherwise, int style, int size,
                                 String[] families) {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        Arrays.sort(fonts);
        Font font = null;
        for (int i = 0; i < families.length; i++) {
            if (Arrays.binarySearch(fonts, families[i]) >= 0) {
                font = new Font(families[i], style, size);
                break;
            }
        }
        if (font == null)
            font = new Font(otherwise, style, size);
        return font;
    }

    private String getString(String key) {
        return plugin.getString(key);
    }

    public JComponent createComponent() {
        JPanel panel = new JPanel();
        JPanel console = new JPanel();
        panel.setLayout(new BorderLayout());

        final JEditorPane text = new JTextPane();

        text.setMargin(new Insets(8, 8, 8, 8));
        text.setCaretColor(new Color(0xa4, 0x00, 0x00));
        text.setBackground(new Color(0xf2, 0xf2, 0xf2));
        text.setForeground(new Color(0xa4, 0x00, 0x00));
        Font font = findFont("Monospaced", Font.PLAIN, 14, new String[]{
                "Monaco", "Andale Mono"});

        text.setFont(font);
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(text);
        pane.setBorder(BorderFactory.createLineBorder(Color.darkGray));
        panel.add(pane, BorderLayout.CENTER);
        console.validate();

        final TextAreaReadline tar = new TextAreaReadline(text,
                getString("Wellcom") + " \n\n");

        RubyInstanceConfig config = new RubyInstanceConfig() {
            {
                //setInput(tar.getInputStream());
                //setOutput(new PrintStream(tar.getOutputStream()));
                //setError(new PrintStream(tar.getOutputStream()));
                setObjectSpaceEnabled(false);
            }
        };
        Ruby runtime = Ruby.newInstance(config);
        tar.hookIntoRuntimeWithStreams(runtime);

        run(runtime);
        return panel;
    }

}

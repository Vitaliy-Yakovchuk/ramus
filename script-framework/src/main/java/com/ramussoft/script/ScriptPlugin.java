package com.ramussoft.script;

import java.awt.event.ActionEvent;

import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jsyntaxpane.DefaultSyntaxKit;

import org.mozilla.javascript.tools.debugger.Main;
import org.mozilla.javascript.tools.debugger.SwingGui;
import org.mozilla.javascript.tools.shell.Global;

import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.ActionLevel;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.TabbedEvent;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.table.TabbedTableView;

public class ScriptPlugin extends AbstractViewPlugin {

    private static final ResourceBundle bundle = ResourceBundle
            .getBundle("com.ramussoft.script.shell");

    private static Main main;

    static {
        DefaultSyntaxKit.initKit();
    }

    // private static RubyConsole rubyConsole;

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor javaScript = new ActionDescriptor();
        javaScript.setActionLevel(ActionLevel.GLOBAL);
        javaScript.setAction(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -6161853134240219376L;

            {
                putValue(ACTION_COMMAND_KEY, "JavaScriptConsole");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (main == null) {
                            main = new Main("Rhino JavaScript Debugger");
                            main.doBreak();

                            main.getDebugFrame().setDefaultCloseOperation(
                                    JFrame.HIDE_ON_CLOSE);

                            ((SwingGui) main.getDebugFrame())
                                    .setExitAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            main.setVisible(false);
                                            Options.saveOptions(main
                                                    .getDebugFrame());
                                        }
                                    });

                            System.setIn(main.getIn());
                            System.setOut(main.getOut());
                            System.setErr(main.getErr());

                            Global global = org.mozilla.javascript.tools.shell.Main
                                    .getGlobal();
                            global.setIn(main.getIn());
                            global.setOut(main.getOut());
                            global.setErr(main.getErr());

                            main
                                    .attachTo(org.mozilla.javascript.tools.shell.Main.shellContextFactory);

                            main.setScope(global);

                            main.pack();
                            main.getDebugFrame().setMinimumSize(
                                    main.getDebugFrame().getSize());
                            main.setSize(600, 460);
                            Options.loadOptions(main.getDebugFrame());

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    org.mozilla.javascript.tools.shell.Main
                                            .exec(new String[]{});
                                    Options.saveOptions(main.getDebugFrame());
                                }
                            }).start();

                        }
                        main.setVisible(true);
                    }
                });
            }
        });

        javaScript.setMenu("Tools");

		/*
         * ActionDescriptor ruby = new ActionDescriptor();
		 * ruby.setActionLevel(ActionLevel.GLOBAL); ruby.setAction(new
		 * AbstractAction() {
		 * 
		 * private static final long serialVersionUID = 9004972326340291462L;
		 * 
		 * { putValue(ACTION_COMMAND_KEY, getString("IRBConsole")); }
		 * 
		 * @Override public void actionPerformed(ActionEvent e) { if
		 * (rubyConsole == null) { rubyConsole = new
		 * RubyConsole(ScriptPlugin.this); rubyConsole.pack();
		 * rubyConsole.setMinimumSize(rubyConsole.getSize()); }
		 * rubyConsole.setVisible(true); } });
		 * 
		 * ruby.setMenu("Tools");
		 */

        return new ActionDescriptor[]{javaScript};
    }

    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return GlobalResourcesManager.getString(key);
        }
    }

    public String getName() {
        return "Scripting";
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    @Override
    public UniqueView[] getUniqueViews() {
        return new UniqueView[]{new JSModulesEditor(framework)};
    }

    @Override
    public void setFramework(final GUIFramework framework) {
        super.setFramework(framework);
        framework.addActionListener("OpenScript", new ActionListener() {

            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                if (framework.openView(event))
                    return;
                TabbedEvent tEvent = new TabbedEvent(
                        TabbedTableView.MAIN_TABBED_VIEW,
                        new ScriptEditor(framework, (String) event
                                .getValue()) {
                            @Override
                            public void close() {
                                super.close();
                                TabbedEvent tEvent = new TabbedEvent(
                                        TabbedTableView.MAIN_TABBED_VIEW, this);
                                tabRemoved(tEvent);
                            }
                        });
                tabCreated(tEvent);
            }
        });
    }

}

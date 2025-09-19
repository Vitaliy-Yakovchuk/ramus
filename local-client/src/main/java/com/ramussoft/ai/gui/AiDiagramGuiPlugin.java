package com.ramussoft.ai.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.ramussoft.ai.AiConfig;
import com.ramussoft.ai.AiDiagramPlugin;
import com.ramussoft.ai.AiDiagramService;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.ActionLevel;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.idef0.IDEF0ViewPlugin;
import com.ramussoft.idef0.OpenDiagram;

/**
 * GUI plugin exposing the AI diagram generation action inside the Tools menu.
 */
public class AiDiagramGuiPlugin extends AbstractViewPlugin {

    private static final long serialVersionUID = 1L;

    private static final String ACTION_KEY = "AiDiagram.Generate";

    private final Action generateAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        {
            putValue(ACTION_COMMAND_KEY, ACTION_KEY);
            putValue(Action.NAME, getString(ACTION_KEY));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openDialog();
        }
    };

    private OpenDiagram currentDiagram;

    @Override
    public String getName() {
        return "AI Diagram";
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor descriptor = new ActionDescriptor();
        descriptor.setActionLevel(ActionLevel.GLOBAL);
        descriptor.setMenu("Tools");
        descriptor.setAction(generateAction);
        return new ActionDescriptor[]{descriptor};
    }

    @Override
    public void setFramework(final GUIFramework framework) {
        super.setFramework(framework);
        framework.addActionListener(IDEF0ViewPlugin.ACTIVE_DIAGRAM, new ActionListener() {
            @Override
            public void onAction(com.ramussoft.gui.common.event.ActionEvent event) {
                Object value = event.getValue();
                if (value instanceof OpenDiagram) {
                    currentDiagram = (OpenDiagram) value;
                } else {
                    currentDiagram = null;
                }
            }
        });
        framework.addActionListener(ACTION_KEY, new ActionListener() {
            @Override
            public void onAction(com.ramussoft.gui.common.event.ActionEvent event) {
                openDialog();
            }
        });
    }

    private void openDialog() {
        if (framework == null) {
            return;
        }
        Engine engine = framework.getEngine();
        AiDiagramPlugin plugin = AiDiagramPlugin.getPlugin(engine);
        if (plugin == null) {
            JOptionPane.showMessageDialog(framework.getMainFrame(),
                    "AI плагин недоступен", "AI Diagram", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!plugin.isConfigured()) {
            JOptionPane.showMessageDialog(framework.getMainFrame(),
                    "Укажите ключ OpenRouter в conf/ramus-ai.conf и переменных окружения.",
                    "AI Diagram", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AiDiagramService service = plugin.getService();
        AiConfig config = plugin.getConfig();
        if (service == null || config == null) {
            JOptionPane.showMessageDialog(framework.getMainFrame(),
                    "Сервис OpenRouter не инициализирован", "AI Diagram",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Allow generation from scratch: if no diagram is open, use base function as target
        OpenDiagram target = (currentDiagram != null) ? currentDiagram : new OpenDiagram(null, -1l);
        AiDiagramDialog dialog = new AiDiagramDialog(framework.getMainFrame(), framework,
                service, config, target);
        dialog.setVisible(true);
    }
}

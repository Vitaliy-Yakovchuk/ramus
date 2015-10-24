package com.ramussoft.client.log;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import com.ramussoft.client.ClientPlugin;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.logger.EngineLogExtension;
import com.ramussoft.common.logger.Event;
import com.ramussoft.common.logger.ILog;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.gui.attribute.TableEditorActionModifier;
import com.ramussoft.gui.attribute.table.TableEditor;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.table.ElementActionPlugin;
import com.ramussoft.gui.qualifier.table.TableTabView;

public class LogPlugin extends AbstractViewPlugin implements
        ElementActionPlugin, TableEditorActionModifier {

    @Override
    public String getName() {
        return "Log";
    }

    @Override
    public Action[] getActions(final TableTabView tableView) {

        return new Action[]{new AbstractAction("EventType.qualifierLog", new ImageIcon(
                getClass().getResource("/com/ramussoft/client/log/log.png"))) {

            /**
             *
             */
            private static final long serialVersionUID = 8698485078278772063L;

            {
                putValue(LONG_DESCRIPTION, getString("EventType.qualifierLog"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                GUIFramework framework = tableView.getFramework();

                long qId = tableView.getQualifier().getId();

                List<Event> events = ((ILog) framework.getEngine())
                        .getEventsWithParams(
                                new String[]{EngineLogExtension.QUALIFIER_ID},
                                new Object[]{qId}, 150);

                ElementLogDialog dialog = new ElementLogDialog(events,
                        framework.getEngine());
                dialog.setSize(800, 600);
                dialog.setLocationRelativeTo(null);
                dialog.setTitle(getString("EventType.qualifierLog"));

                Options.loadOptions(dialog);

                dialog.setVisible(true);

                Options.saveOptions(dialog);
            }
        }};
    }

    @Override
    public String getString(String key) {
        try {
            return ClientPlugin.bundle.getString(key);
        } catch (Exception e) {
            return super.getString(key);
        }
    }

    @Override
    public Action[] modify(Action[] actions, final TableEditor tableEditor) {
        actions = Arrays.copyOf(actions, actions.length + 1);
        actions[actions.length - 1] = new AbstractAction("Log", new ImageIcon(
                getClass().getResource("/com/ramussoft/client/log/log.png"))) {

            /**
             *
             */
            private static final long serialVersionUID = 8698485078278772063L;

            {
                putValue(LONG_DESCRIPTION, getString("EventType.qualifierLog"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                GUIFramework framework = tableEditor.getFramework();

                Engine engine = framework.getEngine();
                long qId = StandardAttributesPlugin
                        .getTableQualifierForAttribute(engine,
                                tableEditor.getAttribute()).getId();

                List<Event> events = ((ILog) engine).getEventsWithParams(
                        new String[]{EngineLogExtension.QUALIFIER_ID},
                        new Object[]{qId}, 750);

                List<Event> events2 = new ArrayList<Event>();

                Attribute attribute = StandardAttributesPlugin
                        .getTableElementIdAttribute(engine);

                Element el = tableEditor.getElement();

                for (Event event : events) {
                    Long elementId = (Long) event.getAttribute("element_id");
                    if (elementId != null) {
                        Element element = engine.getElement(elementId);
                        if (element != null) {
                            Long l = (Long) engine.getAttribute(element,
                                    attribute);
                            if (l != null && l.equals(el.getId()))
                                events2.add(event);
                        }
                    }
                }

                ElementLogDialog dialog = new ElementLogDialog(events2, engine);
                dialog.setSize(800, 600);
                dialog.setLocationRelativeTo(null);
                dialog.setTitle(getString("EventType.qualifierLog"));

                Options.loadOptions(dialog);

                dialog.setVisible(true);

                Options.saveOptions(dialog);
            }
        };
        return actions;
    }

}

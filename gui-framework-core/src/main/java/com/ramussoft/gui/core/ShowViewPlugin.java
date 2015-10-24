package com.ramussoft.gui.core;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.ramussoft.gui.common.AbstractGUIPluginFactory;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionListener;

public class ShowViewPlugin extends AbstractViewPlugin {

    private List<UniqueView> views;

    private AbstractGUIPluginFactory factory;

    @Override
    public void setFramework(final GUIFramework framework) {
        super.setFramework(framework);
        framework.addActionListener(
                com.ramussoft.gui.common.event.ActionEvent.OPEN_STATIC_VIEW,
                new ActionListener() {
                    @Override
                    public void onAction(
                            com.ramussoft.gui.common.event.ActionEvent event) {
                        framework.openView(event);
                    }
                });
    }

    public ShowViewPlugin(List<UniqueView> views, AbstractGUIPluginFactory factory) {
        this.views = views;
        this.factory = factory;
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor[] descriptors = new ActionDescriptor[views.size()];
        for (int i = 0; i < descriptors.length; i++) {
            final UniqueView view = views.get(i);
            ActionDescriptor descriptor = new ActionDescriptor();
            descriptors[i] = descriptor;
            Action action = new AbstractAction() {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    framework
                            .propertyChanged(
                                    com.ramussoft.gui.common.event.ActionEvent.OPEN_STATIC_VIEW,
                                    view.getId());
                }
            };

            action.putValue(Action.ACTION_COMMAND_KEY, factory
                    .findPluginForViewId(view.getId()).getString(view.getId()));
            descriptor.setAction(action);
            descriptor.setMenu("Windows/ShowView");
        }
        return descriptors;
    }

    @Override
    public String getName() {
        return "ShowView";
    }

}

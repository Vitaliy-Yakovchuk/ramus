package com.ramussoft.idef0;

import static com.ramussoft.pb.data.AbstractDataPlugin.PROPERTIES;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.PopupTrigger;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.core.GUIPluginFactory;
import com.ramussoft.gui.qualifier.table.TableView;
import com.ramussoft.pb.DataPlugin;

public class RolesView extends AbstractUniqueView implements UniqueView {

    private Action[] activeActions = new Action[]{};

    private JPanel contentPanel;

    private TableView tableView;

    private JComponent centerComponent;

    public RolesView(GUIFramework framework) {
        super(framework);
    }

    @Override
    public String getString(String key) {
        return ResourceLoader.getString(key);
    }

    @Override
    public String getId() {
        return "RolesView";
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.IDEF0";
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.WEST;
    }

    @Override
    public Action[] getActions() {
        Action[] acns = new Action[activeActions.length + 1];
        acns[0] = selectActiveRole;
        for (int i = 1; i < acns.length; i++)
            acns[i] = activeActions[i - 1];
        return acns;
    }

    private Action selectActiveRole = new AbstractAction(
            getString("Owners.Clasificator"), new ImageIcon(getClass()
            .getResource("/images/roles.png"))) {

        {
            putValue(SHORT_DESCRIPTION, getString("Owners.Clasificator"));
            putValue(SMALL_ICON,
                    new ImageIcon(getClass().getResource("/images/roles.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        {
            putValue(GUIPluginFactory.POPUP_MENU, new PopupTrigger() {

                @Override
                public Action[] getPopupActions() {
                    String property = getProperty(DataPlugin.PROPERTY_OUNERS);
                    if (property == null)
                        property = "";
                    final StringTokenizer st = new StringTokenizer(property,
                            " ");
                    List<Qualifier> owners = new ArrayList<Qualifier>();
                    while (st.hasMoreElements()) {
                        Qualifier owner = getEngine().getQualifier(
                                Long.parseLong(st.nextToken()));
                        if (owner != null)
                            owners.add(owner);
                    }

                    Action[] actions = new Action[owners.size()];

                    for (int i = 0; i < actions.length; i++) {
                        final Qualifier q = owners.get(i);
                        actions[i] = new AbstractAction(owners.get(i)
                                .toString()) {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                actionsRemoved(activeActions);
                                if (tableView != null) {
                                    tableView.close();
                                    tableView = null;
                                    contentPanel.remove(centerComponent);
                                }
                                createCenterPanel(q);

                                contentPanel.revalidate();
                                contentPanel.repaint();
                            }
                        };
                    }

                    return actions;
                }
            });
        }
    };

    @Override
    public JComponent createComponent() {
        contentPanel = new JPanel(new BorderLayout());
        String property = getProperty(DataPlugin.PROPERTY_OUNERS);
        if (property == null)
            property = "";
        final StringTokenizer st = new StringTokenizer(property, " ");
        List<Qualifier> owners = new ArrayList<Qualifier>();
        while (st.hasMoreElements()) {
            Qualifier owner = getEngine().getQualifier(
                    Long.parseLong(st.nextToken()));
            if (owner != null)
                owners.add(owner);
        }

        if (owners.size() > 0)
            createCenterPanel(owners.get(0));
        return contentPanel;
    }

    private class Roles extends TableView {

        public Roles(GUIFramework framework, Qualifier qualifier) {
            super(framework, qualifier);
        }

        @Override
        public String getTitle() {
            return "Roles Title";
        }
    }

    private void createCenterPanel(Qualifier qualifier) {
        if (qualifier != null) {
            tableView = new Roles(framework, qualifier);
            centerComponent = tableView.createComponent();
            contentPanel.add(centerComponent, BorderLayout.CENTER);
            activeActions = tableView.getActions();
            actionsAdded(activeActions);
        }
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        final InputStream is = getNamedData(PROPERTIES);
        if (is != null) {
            try {
                properties.loadFromXML(is);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public void setProperty(final String key, final String value) {
        Properties properties = getProperties();
        properties.setProperty(key, value);
        final OutputStream out = setNamedData(PROPERTIES);
        try {
            properties.storeToXML(out, "");
            out.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(final String key) {
        return getProperties().getProperty(key);
    }

    public OutputStream setNamedData(final String name) {
        return getEngine().getOutputStream(name);
    }

    public InputStream getNamedData(final String name) {
        return getEngine().getInputStream(name);
    }

    private Engine getEngine() {
        return framework.getEngine();
    }
}

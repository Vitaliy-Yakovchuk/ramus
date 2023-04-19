package com.ramussoft.gui.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.table.DefaultTableModel;

import com.ramussoft.common.Metadata;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class AboutDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 2259997170092758726L;

    private List<Plugin> plugins;

    private List<GUIPlugin> guiPlugins;

    public AboutDialog(JFrame frame, List<Plugin> plugins,
                       List<GUIPlugin> guiPlugins) {
        super(frame, true);
        this.setTitle(GlobalResourcesManager.getString("About"));
        this.plugins = plugins.subList(0, plugins.size());
        this.guiPlugins = guiPlugins;
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JTabbedPane pane = new JTabbedPane();
        pane.addTab(GlobalResourcesManager.getString("About.MainTab"),
                createAboutComponent());
        pane.addTab(GlobalResourcesManager.getString("About.PluginList"),
                createPluginListComponent());
        pane.addTab(GlobalResourcesManager.getString("About.GUIPluginList"),
                createGUIPluginListComponent());

        pane.addTab(GlobalResourcesManager.getString("About.ThirdParts"),
                createThirdPartsComponnt());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(pane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(new JButton(new AbstractAction(GlobalResourcesManager
                .getString("ok")) {
            /**
             *
             */
            private static final long serialVersionUID = -8334150633007409370L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }));
        panel.add(bottomPanel, BorderLayout.SOUTH);
        this.setContentPane(panel);
        setSize(600, 350);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private Component createThirdPartsComponnt() {
        JScrollPane pane = new JScrollPane();
        final JTextArea area = new JTextArea();
        area.setWrapStyleWord(true);
        area.setFont(new Font("Sans Serif", 0, area.getFont().getSize()));
        pane.setViewportView(area);
        area.setEditable(false);
        InputStream is = getClass().getResourceAsStream(
                "/com/ramussoft/gui/core/libraries.txt");
        try {
            byte[] bs = new byte[is.available()];
            is.read(bs);
            is.close();
            area.setText(new String(bs, "UTF8"));
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    area.scrollRectToVisible(new Rectangle(0, 0, 40, 40));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pane;
    }

    private Component createGUIPluginListComponent() {
        JScrollPane pane = new JScrollPane();
        Object[][] data = new Object[guiPlugins.size()][];
        for (int i = 0; i < guiPlugins.size(); i++) {
            GUIPlugin plugin = guiPlugins.get(i);
            String name = plugin.getName();
            if (plugin instanceof com.ramussoft.gui.common.AttributePlugin)
                name = "Attribute."
                        + name
                        + "."
                        + ((com.ramussoft.gui.common.AttributePlugin) plugin)
                        .getAttributeType().getTypeName();
            data[i] = new Object[]{name};
        }

        Arrays.sort(data, new Comparator<Object[]>() {

            @SuppressWarnings("unchecked")
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Comparable<String>) o1[0]).compareTo((String) o2[0]);
            }

        });

        DefaultTableModel model = new DefaultTableModel(
                data,
                new Object[]{GlobalResourcesManager.getString("Plugin.Name")}) {
            /**
             *
             */
            private static final long serialVersionUID = 4893341040484525590L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pane.setViewportView(new JTable(model));
        return pane;
    }

    private Component createPluginListComponent() {
        JScrollPane pane = new JScrollPane();
        Object[][] data = new Object[plugins.size()][];
        for (int i = 0; i < plugins.size(); i++) {
            Plugin plugin = plugins.get(i);
            String name = plugin.getName();
            if (plugin instanceof AttributePlugin)
                name = "Attribute." + name + "."
                        + ((AttributePlugin) plugin).getTypeName();
            data[i] = new Object[]{name};
        }

        Arrays.sort(data, new Comparator<Object[]>() {

            @SuppressWarnings("unchecked")
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Comparable<String>) o1[0]).compareTo((String) o2[0]);
            }

        });

        DefaultTableModel model = new DefaultTableModel(
                data,
                new Object[]{GlobalResourcesManager.getString("Plugin.Name")}) {
            /**
             *
             */
            private static final long serialVersionUID = -1986847073145962545L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pane.setViewportView(new JTable(model));
        return pane;
    }

    private Component createAboutComponent() {
        JScrollPane pane = new JScrollPane();
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText(getAboutText());
        textPane.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == EventType.ACTIVATED) {
                    try {
                        if (e.getDescription().startsWith("mailto:")) {
                            URI mailtoURI = new URI(e.getDescription());
                            Desktop.getDesktop().mail(mailtoURI);
                        } else {
                            Desktop.getDesktop().browse(
                                    new URI(e.getURL().toString()));
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        });
        pane.setViewportView(textPane);
        return pane;
    }

    @SuppressWarnings("unused")
    private String getAboutText() {
        StringBuffer sb = new StringBuffer();
        String applicationName = Metadata.getApplicationName();
        if (Metadata.DEMO || Metadata.CLIENT) {
            if (!Metadata.DEMO_REGISTERED)
                applicationName += " "
                        + GlobalResourcesManager.getString("UnregisteredCopy");
            else if (Metadata.DEMO || Metadata.CLIENT)
                applicationName += " "
                        + MessageFormat.format(GlobalResourcesManager
                                .getString("RegisteredName"),
                        Metadata.REGISTERED_FOR);

        }
        sb.append("<html><body><font face=\"Sans Serif\">")
                .append(applicationName).append(" <br><br>Version: ");
        sb.append(Metadata.getApplicationVersion());
        sb.append("<br><br>");

        sb.append("Copyright &copy; 2005 - 2023 Vitaliy Yakovchuk, Oleksiy Chizhevskiy. <br><br>"
                + "License <a href=\"https://www.gnu.org/licenses/gpl-3.0.en.html\">GNU GENERAL PUBLIC LICENSE Version 3</a><br><br>"
                + "Visit <a href=\"http://ramussoftware.com/\">http://ramussoftware.com/</a><br><br>"
                + "</font></body></html>");
        return sb.toString();
    }

    @Override
    public void dispose() {
        plugins = null;
        guiPlugins = null;
        super.dispose();
    }
}

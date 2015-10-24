package com.ramussoft.gui.qualifier.table;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.ramussoft.common.DeleteStatus;
import com.ramussoft.common.DeleteStatusList;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class StatusMessageFormat {

    public static String toMessage(DeleteStatusList list, GUIFramework framework) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><body>");
        int i = 0;
        for (DeleteStatus status : list) {
            sb.append(format(status.getPluginAnswer(), framework, status
                    .getPluginName()));
            i++;
            if (i < list.size())
                sb.append("<hr>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String format(String pluginAnswer, GUIFramework framework,
                                 String pluginName) {
        GUIPlugin plugin = framework.findPlugin(pluginName);
        String[] keys = findKeys(pluginAnswer);
        for (String key : keys) {
            String k = "\\{" + key + "\\}";
            String v;
            if (plugin == null) {
                v = GlobalResourcesManager.getString(key);
                if (v == null)
                    v = key;
            } else
                v = plugin.getString(key);
            pluginAnswer = pluginAnswer.replaceAll(k, v);
        }
        return pluginAnswer;
    }

    private static String[] findKeys(String pluginAnswer) {
        ArrayList<String> keys = new ArrayList<String>(1);
        StringBuffer sb = new StringBuffer();
        int i = 0;
        boolean inKey = false;
        while (i < pluginAnswer.length()) {
            char c = pluginAnswer.charAt(i);
            if (inKey) {
                if (c == '}') {
                    inKey = false;
                    keys.add(sb.toString());
                    sb = new StringBuffer();
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '{') {
                    inKey = true;
                }
            }
            i++;
        }
        return keys.toArray(new String[keys.size()]);
    }

    public static boolean deleteElements(DeleteStatusList list,
                                         JComponent component, GUIFramework framework) {

        final JLabel label = new JLabel(toMessage(list, framework));
        final JScrollPane pane = new JScrollPane();
        pane.setBorder(null);
        pane.setViewportView(label);
        final Dimension d = label.getPreferredSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        boolean b = false;
        if (d.width >= screen.width * 0.7) {
            d.width = (int) (screen.width * 0.7);
            b = true;
        } else
            d.width += 20;
        if (d.height >= screen.height / 2) {
            d.height = screen.height / 2;
            b = true;
        } else
            d.height += 20;
        if (b)
            pane.setPreferredSize(d);

        int r = JOptionPane.showOptionDialog(component, new Object[]{
                        pane,
                        new JLabel(GlobalResourcesManager
                                .getString("DeleteActiveElementsDialog.Warning"))},
                GlobalResourcesManager.getString("ConfirmMessage.Title"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                null, null);

        if (JOptionPane.YES_OPTION == r) {
            return true;
        }
        return false;
    }
}

package com.ramussoft.gui.core;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.ActionLevel;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public class LookAndFeelPlugin extends AbstractViewPlugin {

    @Override
    public String getName() {
        return "LookAndFeel";
    }

    @Override
    public String getString(String key) {
        try {
            return GlobalResourcesManager.getString(key);
        } catch (NullPointerException e) {
            return key;
        }
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
        ActionDescriptor[] res = new ActionDescriptor[infos.length];
        final LookAndFeel laf = UIManager.getLookAndFeel();
        for (int i = 0; i < infos.length; i++) {
            final LookAndFeelInfo info = infos[i];
            ActionDescriptor ad = new ActionDescriptor();

            ad.setActionLevel(ActionLevel.GLOBAL);
            ad.setButtonGroup("LookAndFeel");
            ad.setMenu("Windows/LookAndFeel");
            ad.setSelective(true);

            AbstractAction action = new AbstractAction() {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    Options.setString("LookAndFeel", info.getClassName());
                    System.setProperty("AnywayExit", Boolean.TRUE.toString());
                    JOptionPane
                            .showMessageDialog(
                                    framework.getMainFrame(),
                                    GlobalResourcesManager
                                            .getString("LookAndFeelWillApplyAfterProgramReboot"));
                }
            };

            action.putValue(Action.SELECTED_KEY, (info.getClassName()
                    .equals(laf.getClass().getName())));
            action.putValue(Action.ACTION_COMMAND_KEY, info.getName());
            ad.setAction(action);

            res[i] = ad;
        }
        return res;
    }
}

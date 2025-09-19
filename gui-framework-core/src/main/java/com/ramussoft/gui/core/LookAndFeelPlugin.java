package com.ramussoft.gui.core;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

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
import com.ramussoft.gui.core.laf.IOS26LookAndFeel;

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
        IOS26LookAndFeel.install();

        LookAndFeelInfo[] installed = UIManager.getInstalledLookAndFeels();
        ArrayList<LookAndFeelInfo> infoList = new ArrayList<LookAndFeelInfo>();
        for (int i = 0; i < installed.length; i++) {
            infoList.add(installed[i]);
        }
        boolean hasIOS = false;
        for (LookAndFeelInfo info : infoList) {
            if (IOS26LookAndFeel.class.getName().equals(info.getClassName())) {
                hasIOS = true;
                break;
            }
        }
        if (!hasIOS) {
            infoList.add(new LookAndFeelInfo(IOS26LookAndFeel.NAME,
                    IOS26LookAndFeel.class.getName()));
        }

        LookAndFeelInfo[] infos = infoList
                .toArray(new LookAndFeelInfo[infoList.size()]);
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

package com.ramussoft.pb.idef.elements;

import java.text.MessageFormat;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class DemoChecker extends ElementAdapter {

    private GUIFramework framework;

    public DemoChecker(GUIFramework framework) {
        this.framework = framework;
        IEngineImpl impl = (IEngineImpl) framework.getEngine().getDeligate();
        String prefix = impl.getPrefix();
        long elementCount = getElementCount(impl, prefix);
        if (elementCount > 100) {
            framework.propertyChanged("DisableSaveActions");
        }
        if ((elementCount >= 101) && (framework.get("FilePlugin") != null)) {

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(DemoChecker.this.framework
                            .getMainFrame(), MessageFormat.format(
                            GlobalResourcesManager
                                    .getString("DemoVersionElementCountLimit"),
                            100));
                }
            });
        }
    }

    @Override
    public void elementCreated(ElementEvent event) {
        IEngineImpl impl = (IEngineImpl) event.getEngine().getDeligate();
        String prefix = impl.getPrefix();
        long elementCount = getElementCount(impl, prefix);
        if (elementCount > 100) {
            framework.propertyChanged("DisableSaveActions");
        }
        if ((elementCount == 101) && (framework.get("FilePlugin") != null)) {

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JOptionPane.showMessageDialog(framework.getMainFrame(),
                            MessageFormat.format(GlobalResourcesManager
                                            .getString("DemoVersionElementCountLimit"),
                                    100));
                }
            });
        }

    }

    public static long getElementCount(IEngineImpl impl, String prefix) {
        return impl
                .getTemplate()
                .queryForLong(
                        "SELECT COUNT(*) FROM "
                                + prefix
                                + "elements WHERE QUALIFIER_ID IN(SELECT QUALIFIER_ID from "
                                + prefix
                                + "qualifiers WHERE QUALIFIER_SYSTEM=FALSE)");
    }

    @Override
    public void elementDeleted(ElementEvent event) {
        IEngineImpl impl = (IEngineImpl) event.getEngine().getDeligate();
        String prefix = impl.getPrefix();
        long elementCount = getElementCount(impl, prefix);
        if (elementCount <= 100) {
            framework.propertyChanged("EnableSaveActions");
        }
    }

    public GUIFramework getFramework() {
        return framework;
    }

}

package com.ramussoft.pb.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;

import com.dsoft.pb.idef.ResourceLoader;

/**
 * Клас призначения для роботи з довідковою ситемою до програми.
 *
 * @author Яковчук В.В. (zdd)
 */

public class Helper {

    private ActionListener showHelpContext = null;

    private final String helpHS;

    public Helper(final String HS) {
        helpHS = HS;
    }

    public void showHelpContext(final ActionEvent e) {
        getShowHelpContext().actionPerformed(e);
    }

    private ActionListener getShowHelpContext() {
        if (showHelpContext == null) {

            HelpSet hs = null;
            try {
                final URL hsURL = getClass().getResource("/" + helpHS);
                hs = new HelpSet(null, hsURL);
                final HelpBroker hb = hs.createHelpBroker();
                hb.setLocale(ResourceLoader.getLocale());
                showHelpContext = new CSH.DisplayHelpFromSource(hb);
            } catch (final Exception ee) {
                ee.printStackTrace();
            }

        }
        return showHelpContext;
    }

}

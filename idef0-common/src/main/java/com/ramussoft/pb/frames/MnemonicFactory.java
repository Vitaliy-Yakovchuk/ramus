package com.ramussoft.pb.frames;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MnemonicFactory {

    public static void setMnemonics(final JMenuBar menuBar) {
        final int c = menuBar.getMenuCount();
        final ArrayList<JMenuItem> list = new ArrayList<JMenuItem>(c);
        for (int i = 0; i < c; i++)
            list.add(menuBar.getMenu(i));
        setMnemonics(list);
    }

    private static void setMnemonics(final ArrayList<JMenuItem> list) {
        final ArrayList<Character> c = new ArrayList<Character>();
        for (final JMenuItem item : list)
            if (item != null) {
                setMnemonics(item);
                final int m = item.getMnemonic();
                if (m == 0) {
                    final Character mn = getM(c, item.getText());
                    if (mn != null) {
                        c.add(mn);
                        item.setMnemonic(mn);
                    }
                } else
                    c.add(new Character((char) m));
            }
    }

    private static Character getM(final ArrayList<Character> cs, final String text) {
        for (int i = 0; i < text.length(); i++) {
            final Character c = text.charAt(i);
            if (cs.indexOf(c) < 0) {
                return c;
            }
        }

        return null;
    }

    private static void setMnemonics(final JMenuItem item) {
        if (item instanceof JMenu) {
            final JMenu menu = (JMenu) item;
            final int c = menu.getMenuComponentCount();
            final ArrayList<JMenuItem> list = new ArrayList<JMenuItem>(c);
            for (int i = 0; i < c; i++) {
                final Component co = menu.getMenuComponent(i);
                if (co instanceof JMenuItem)
                    list.add((JMenuItem) co);

            }
            if (list.size() > 0)
                setMnemonics(list);
        }
    }

}

package com.ramussoft.gui;

import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class GUIPatchFactory {

    public static void patchHTMLTextPane(JTextPane pane) {
        Font font = UIManager.getFont("TextPane.font");

        if (font == null) {
            font = UIManager.getFont("TextArea.font");
        }

        if (font == null) {
            font = UIManager.getFont("Label.font");
        }

        if (font == null) {
            font = new Font("Dialog", Font.PLAIN, 12);
        }

        String bodyRule = "body { font-family: " + font.getFamily() + "; "
                + "font-size: " + font.getSize() + "pt; }";
        StyleSheet styleSheet = ((HTMLDocument) pane.getDocument()).getStyleSheet();

        styleSheet.addRule(bodyRule);
    }

}

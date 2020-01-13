package com.ramussoft.gui.attribute;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import static javax.swing.SwingUtilities.invokeLater;

public class TextPanelWithLinksDetector extends JTextPane {

    private final SimpleAttributeSet linkStyle;
    private final Cursor linkCursor;
    private final ExecutorService linksExecutor;
    private Cursor defautCursor;
    private final Map<Integer, String> links = new LinkedHashMap<Integer, String>();

    public TextPanelWithLinksDetector(final TextAttributePlugin plugin) {
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        this.linkCursor = htmlEditorKit.getLinkCursor();
        this.linkStyle = createLinkStyle();
        this.linksExecutor = Executors.newSingleThreadExecutor();

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                String link = getLink(e.getPoint());
                if (link == null) {
                    if (defautCursor != null) {
                        setCursor(defautCursor);
                    }
                } else {
                    if (defautCursor == null) {
                        defautCursor = getCursor();
                    }
                    setCursor(linkCursor);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String link = getLink(e.getPoint());
                if (link != null) {
                    plugin.openUrl(link);
                }
            }
        });

        getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                applyNewLinks();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                applyNewLinks();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                applyNewLinks();
            }
        });
    }

    private void applyNewLinks() {
        linksExecutor.execute(new Runnable() {
            @Override
            public void run() {
                invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        applyLinkStyles();
                    }
                });
            }
        });

    }

    private String getLink(Point point) {
        int startOffset = viewToModel(point);
        for (Map.Entry<Integer, String> entry : links.entrySet()) {
            if (entry.getKey() > startOffset) {
                return null;
            }
            if (entry.getKey() + entry.getValue().length() >= startOffset) {
                return entry.getValue();
            }
        }
        return null;
    }

    private SimpleAttributeSet createLinkStyle() {
        SimpleAttributeSet linkStyle = new SimpleAttributeSet();

        StyleConstants.setUnderline(linkStyle, true);
        StyleConstants.setBold(linkStyle, true);

        return linkStyle;
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        applyNewLinks();
    }

    private void applyLinkStyles() {
        Map<Integer, String> links = getLinks();
        if (links.equals(this.links)) {
            return;
        }
        this.links.clear();
        this.links.putAll(links);
        StyledDocument kit = (StyledDocument) this.getDocument();
        kit.setCharacterAttributes(0, getText().length(), new SimpleAttributeSet(), true);
        for (Map.Entry<Integer, String> entry : links.entrySet()) {
            kit.setCharacterAttributes(entry.getKey(), entry.getValue().length(), this.linkStyle, true);
        }
    }

    private Map<Integer, String> getLinks() {
        String text = getText();
        Matcher matcher = TextAttributePlugin.urlPattern.matcher(text);
        Map<Integer, String> links = new LinkedHashMap<Integer, String>();
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            links.put(matchStart, text.substring(matchStart, matchEnd));
        }
        return links;
    }
}
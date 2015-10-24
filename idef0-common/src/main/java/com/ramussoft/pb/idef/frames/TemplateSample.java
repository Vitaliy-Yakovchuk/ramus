package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.dmaster.Template;

public class TemplateSample extends JPanel {

    private Template active;

    private int diagramType = 0;

    private final JPanel prev = new JPanel(new BorderLayout()) {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (active != null) {
                if (active.getDecompositionType() != diagramType) {
                    active.setDecompositionType(diagramType);
                    active.refresh();
                    repaint();
                }
                active.paint((Graphics2D) g, getVisibleRect());
            }
        }
    };

    public TemplateSample() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(ResourceLoader
                .getString("sample")));
        this.add(prev, BorderLayout.CENTER);
    }

    public void setActive(final Template active) {
        this.active = active;
    }

    public void setDiagramType(int diagramType) {
        this.diagramType = diagramType;
    }
}

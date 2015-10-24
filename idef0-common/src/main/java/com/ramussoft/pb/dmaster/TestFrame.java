package com.ramussoft.pb.dmaster;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TestFrame extends JFrame {

    public TestFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setContentPane(createContentPanel());
    }

    SimpleTemplate model = new ClassicTemplate();

    private Container createContentPanel() {
        final JPanel panel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                model.paint((Graphics2D) g, this.getBounds());
            }
        };

        final JPanel res = new JPanel(new BorderLayout());
        res.add(panel, BorderLayout.CENTER);
        final JSpinner slider = new JSpinner(
                new SpinnerNumberModel(4, 2, 8, 1) {

                });
        res.add(slider, BorderLayout.SOUTH);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(final ChangeEvent e) {
                model.setCount(((Number) slider.getValue()).intValue());
                TestFrame.this.repaint();
            }

        });
        return res;
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        /*Main.dataPlugin = new NDataPlugin();
        final TestFrame f = new TestFrame();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.setVisible(true);
			}
		});
*/
    }

}

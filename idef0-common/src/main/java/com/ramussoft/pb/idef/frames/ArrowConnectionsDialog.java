package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.DataLoader;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.SectorBorder;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingPanel;
import com.dsoft.pb.types.FRectangle;

/**
 * Dialog that allows managing connections between arrows and a function as well
 * as creating brand new arrows without leaving the diagram.
 */
public class ArrowConnectionsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final MovingArea movingArea;

    private Function targetFunction;

    private final JPanel existingListPanel = new JPanel();
    private final JScrollPane existingScroll;

    private final JTextField nameField = new JTextField(20);
    private final JComboBox<FunctionChoice> sourceFunctionCombo = new JComboBox<>();
    private final JComboBox<FunctionChoice> targetFunctionCombo = new JComboBox<>();
    private final JComboBox<SideOption> sourceSideCombo;
    private final JComboBox<SideOption> targetSideCombo;
    private final JButton createButton = new JButton();

    private final SideOption[] sideOptions = createSideOptions();

    private boolean updating;

    private static int lastSourceSide = MovingPanel.LEFT;
    private static int lastTargetSide = MovingPanel.RIGHT;
    private static Function lastSourceFunction = null;
    private static boolean lastSourceBoundary = true;
    private static Function lastTargetFunction = null;
    private static boolean lastTargetBoundary = false;

    private final List<ArrowEntry> entries = new ArrayList<ArrowEntry>();

    public ArrowConnectionsDialog(JFrame owner, MovingArea movingArea) {
        super(owner, true);
        this.movingArea = movingArea;
        setModal(true);
        setLayout(new BorderLayout(10, 10));

        existingListPanel.setLayout(new BoxLayout(existingListPanel, BoxLayout.Y_AXIS));
        existingScroll = new JScrollPane(existingListPanel);
        existingScroll.setBorder(BorderFactory
                .createTitledBorder(ResourceLoader.getString("ArrowConnections.existing")));
        add(existingScroll, BorderLayout.CENTER);

        sourceSideCombo = new JComboBox<SideOption>(sideOptions.clone());
        targetSideCombo = new JComboBox<SideOption>(sideOptions.clone());

        JPanel createPanel = buildCreationPanel();
        add(createPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(560, 460));
        setMinimumSize(new Dimension(520, 420));
    }

    public void showForFunction(Function function) {
        this.targetFunction = function;
        String title = ResourceLoader.getString("ArrowConnections.title");
        if (function != null && function.getName() != null
                && function.getName().trim().length() > 0)
            title = title + " - " + function.getName();
        setTitle(title);
        populateFunctionCombos();
        nameField.setText("");
        loadArrows();
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private JPanel buildCreationPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory
                .createTitledBorder(ResourceLoader.getString("ArrowConnections.create")));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel(ResourceLoader.getString("ArrowConnections.source")), gbc);

        gbc.gridx = 1;
        panel.add(sourceFunctionCombo, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel(ResourceLoader.getString("ArrowConnections.side")), gbc);

        gbc.gridx = 3;
        panel.add(sourceSideCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel(ResourceLoader.getString("ArrowConnections.target")), gbc);

        gbc.gridx = 1;
        panel.add(targetFunctionCombo, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel(ResourceLoader.getString("ArrowConnections.side")), gbc);

        gbc.gridx = 3;
        panel.add(targetSideCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel(ResourceLoader.getString("ArrowConnections.name")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(nameField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        createButton.setText(ResourceLoader.getString("ArrowConnections.createButton"));
        createButton.addActionListener(this::createArrow);
        panel.add(createButton, gbc);

        container.add(panel, BorderLayout.CENTER);
        container.add(Box.createVerticalStrut(4), BorderLayout.NORTH);
        container.add(Box.createVerticalStrut(4), BorderLayout.SOUTH);

        return container;
    }

    private void populateFunctionCombos() {
        updating = true;
        try {
            DefaultComboBoxModel<FunctionChoice> sourceModel = new DefaultComboBoxModel<FunctionChoice>();
            DefaultComboBoxModel<FunctionChoice> targetModel = new DefaultComboBoxModel<FunctionChoice>();

            for (FunctionChoice choice : buildFunctionChoices()) {
                sourceModel.addElement(choice);
                targetModel.addElement(choice.copy());
            }

            sourceFunctionCombo.setModel(sourceModel);
            targetFunctionCombo.setModel(targetModel);

            selectFunctionChoice(sourceFunctionCombo, lastSourceFunction, lastSourceBoundary);
            if (sourceFunctionCombo.getSelectedItem() == null && sourceModel.getSize() > 0)
                sourceFunctionCombo.setSelectedIndex(0);

            selectFunctionChoice(targetFunctionCombo, targetFunction, false);
            if (targetFunctionCombo.getSelectedItem() == null)
                selectFunctionChoice(targetFunctionCombo, lastTargetFunction, lastTargetBoundary);
            if (targetFunctionCombo.getSelectedItem() == null && targetModel.getSize() > 0)
                targetFunctionCombo.setSelectedIndex(0);

            sourceSideCombo.setModel(new DefaultComboBoxModel<SideOption>(sideOptions.clone()));
            targetSideCombo.setModel(new DefaultComboBoxModel<SideOption>(sideOptions.clone()));

            sourceSideCombo.setSelectedItem(findSideOption(lastSourceSide));
            targetSideCombo.setSelectedItem(findSideOption(lastTargetSide));
        } finally {
            updating = false;
        }
    }

    private List<FunctionChoice> buildFunctionChoices() {
        List<FunctionChoice> choices = new ArrayList<FunctionChoice>();
        choices.add(new FunctionChoice(null, true,
                ResourceLoader.getString("ArrowConnections.boundary")));
        List<Row> rows = movingArea.getDataPlugin().getChilds(movingArea.getActiveFunction(), true);
        for (Row row : rows) {
            if (row instanceof Function) {
                Function function = (Function) row;
                choices.add(new FunctionChoice(function, false,
                        getFunctionDisplayName(function)));
            }
        }
        return choices;
    }

    private String getFunctionDisplayName(Function function) {
        if (function == null)
            return ResourceLoader.getString("ArrowConnections.boundary");
        String name = function.getName();
        if (name != null && name.trim().length() > 0)
            return name;
        return ResourceLoader.getString("ArrowConnections.unnamedFunction");
    }

    private void selectFunctionChoice(JComboBox<FunctionChoice> combo,
            Function function, boolean boundary) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            FunctionChoice choice = combo.getItemAt(i);
            if (choice.matches(function, boundary)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void createArrow(ActionEvent event) {
        if (updating)
            return;
        FunctionChoice sourceChoice = (FunctionChoice) sourceFunctionCombo
                .getSelectedItem();
        FunctionChoice targetChoice = (FunctionChoice) targetFunctionCombo
                .getSelectedItem();
        SideOption sourceSide = (SideOption) sourceSideCombo.getSelectedItem();
        SideOption targetSide = (SideOption) targetSideCombo.getSelectedItem();
        String name = nameField.getText().trim();

        if (sourceChoice == null) {
            showMessage(ResourceLoader
                    .getString("ArrowConnections.validation.source"));
            return;
        }
        if (targetChoice == null) {
            showMessage(ResourceLoader
                    .getString("ArrowConnections.validation.target"));
            return;
        }
        if (sourceSide == null || targetSide == null) {
            showMessage(ResourceLoader
                    .getString("ArrowConnections.validation.side"));
            return;
        }
        if (name.length() == 0) {
            showMessage(ResourceLoader
                    .getString("ArrowConnections.validation.name"));
            return;
        }
        if (!sourceChoice.boundary && !targetChoice.boundary
                && sourceChoice.function != null
                && sourceChoice.function.equals(targetChoice.function)
                && sourceSide.side == targetSide.side) {
            showMessage(ResourceLoader
                    .getString("ArrowConnections.validation.same"));
            return;
        }

        try {
            if (createArrowInternal(sourceChoice, sourceSide, targetChoice,
                    targetSide, name)) {
                lastSourceFunction = sourceChoice.function;
                lastSourceBoundary = sourceChoice.boundary;
                lastTargetFunction = targetChoice.function;
                lastTargetBoundary = targetChoice.boundary;
                lastSourceSide = sourceSide.side;
                lastTargetSide = targetSide.side;
                nameField.setText("");
                loadArrows();
            }
        } catch (Exception ex) {
            showError(ResourceLoader
                    .getString("ArrowConnections.error.create"), ex);
        }
    }

    private boolean createArrowInternal(FunctionChoice sourceChoice,
            SideOption sourceSide, FunctionChoice targetChoice,
            SideOption targetSide, String name) {
        SectorRefactor refactor = movingArea.getRefactor();
        boolean started = movingArea.isUserTransactionStarted();
        if (!started)
            movingArea.startUserTransaction();
        try {
            refactor.setPoint(buildPerspectivePoint(sourceChoice, sourceSide,
                    true, targetChoice.function));
            refactor.setPoint(buildPerspectivePoint(targetChoice, targetSide,
                    false, sourceChoice.function));
            refactor.createNewSector();
            refactor.fixOwners();
            PaintSector sector = refactor.getSector();
            applyStreamName(sector, name);
            PaintSector.save(sector, new DataLoader.MemoryData(), movingArea
                    .getDataPlugin().getEngine());
            refactor.setUndoPoint();
            movingArea.repaintAsync();
            return true;
        } finally {
            if (!started && movingArea.isUserTransactionStarted())
                movingArea.commitUserTransaction();
        }
    }

    private SectorRefactor.PerspectivePoint buildPerspectivePoint(
            FunctionChoice choice, SideOption side, boolean start,
            Function fallbackReference) {
        SectorRefactor.PerspectivePoint point = new SectorRefactor.PerspectivePoint();
        point.type = start ? SectorRefactor.TYPE_START : SectorRefactor.TYPE_END;
        if (!choice.boundary && choice.function != null) {
            point.setFunction(choice.function, side.side);
            Point p = createFunctionPoint(choice.function, side.side);
            point.point = p;
            point.x = p.getX();
            point.y = p.getY();
        } else {
            point.borderType = side.side;
            double[] coords = computeBoundaryCoordinates(side.side,
                    fallbackReference != null ? fallbackReference
                            : targetFunction);
            point.x = coords[0];
            point.y = coords[1];
        }
        return point;
    }

    private Point createFunctionPoint(Function function, int side) {
        FRectangle bounds = function.getBounds();
        double x = bounds.getX() + bounds.getWidth() / 2d;
        double y = bounds.getY() + bounds.getHeight() / 2d;
        switch (side) {
            case MovingPanel.LEFT:
                x = bounds.getX();
                break;
            case MovingPanel.RIGHT:
                x = bounds.getRight();
                break;
            case MovingPanel.TOP:
                y = bounds.getY();
                break;
            case MovingPanel.BOTTOM:
                y = bounds.getBottom();
                break;
            default:
                break;
        }
        Ordinate xOrd = new Ordinate(Ordinate.TYPE_X);
        xOrd.setPosition(x);
        Ordinate yOrd = new Ordinate(Ordinate.TYPE_Y);
        yOrd.setPosition(y);
        Point p = new Point(xOrd, yOrd);
        if (side == MovingPanel.LEFT || side == MovingPanel.RIGHT)
            p.setType(Ordinate.TYPE_X);
        else
            p.setType(Ordinate.TYPE_Y);
        return p;
    }

    private double[] computeBoundaryCoordinates(int side, Function reference) {
        FRectangle refBounds = reference != null ? reference.getBounds()
                : movingArea.getActiveFunction() != null
                        ? movingArea.getActiveFunction().getBounds()
                        : new FRectangle();
        double centerX = refBounds.getX() + refBounds.getWidth() / 2d;
        double centerY = refBounds.getY() + refBounds.getHeight() / 2d;
        return new double[]{centerX, centerY};
    }

    private void applyStreamName(PaintSector sector, String name) {
        Stream stream = sector.getStream();
        if (stream == null || !stream.isEmptyName()) {
            stream = (Stream) movingArea.getDataPlugin().createRow(
                    movingArea.getDataPlugin().getBaseStream(), true);
        }
        stream.setName(name);
        stream.setEmptyName(false);
        sector.setStream(stream, ReplaceStreamType.CHILDREN);
        sector.setShowText(true);
        sector.createTexts();
    }

    private void loadArrows() {
        updating = true;
        try {
            entries.clear();
            existingListPanel.removeAll();

            List<PaintSector> sectors = new ArrayList<PaintSector>(
                    movingArea.getRefactor().getSectors());
            Collections.sort(sectors,
                    Comparator.comparing(this::getArrowDisplayName,
                            String.CASE_INSENSITIVE_ORDER));

            for (PaintSector sector : sectors) {
                if (!isEligible(sector))
                    continue;
                SectorBorder endBorder = sector.getSector().getEnd();
                boolean connected = endBorder.getFunction() != null
                        && endBorder.getFunction().equals(targetFunction);
                int side = connected ? endBorder.getFunctionType()
                        : lastTargetSide;
                ArrowEntry entry = new ArrowEntry(sector,
                        getArrowDisplayName(sector), connected,
                        side >= 0 ? side : MovingPanel.LEFT);
                entries.add(entry);
                existingListPanel.add(entry.getPanel());
            }

            if (entries.isEmpty()) {
                JLabel label = new JLabel(
                        ResourceLoader.getString("ArrowConnections.noArrows"));
                label.setAlignmentX(LEFT_ALIGNMENT);
                existingListPanel.add(label);
            }
            existingListPanel.revalidate();
            existingListPanel.repaint();
        } finally {
            updating = false;
        }
    }

    private boolean isEligible(PaintSector sector) {
        SectorBorder startBorder = sector.getSector().getStart();
        if (startBorder != null && startBorder.getFunction() != null
                && startBorder.getFunction().equals(targetFunction))
            return false;
        return true;
    }

    private String getArrowDisplayName(PaintSector sector) {
        Stream stream = sector.getStream();
        if (stream != null) {
            String name = stream.getName();
            if (name != null && name.trim().length() > 0)
                return name.trim();
        }
        return ResourceLoader.getString("ArrowConnections.unnamed");
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message,
                ResourceLoader.getString("warning"),
                JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, message,
                ResourceLoader.getString("warning"),
                JOptionPane.ERROR_MESSAGE);
    }

    private SideOption[] createSideOptions() {
        return new SideOption[]{new SideOption(MovingPanel.LEFT),
                new SideOption(MovingPanel.RIGHT),
                new SideOption(MovingPanel.TOP),
                new SideOption(MovingPanel.BOTTOM)};
    }

    private SideOption findSideOption(int side) {
        for (SideOption option : sideOptions)
            if (option.side == side)
                return option;
        return sideOptions[0];
    }

    private boolean connectArrow(PaintSector sector, int side) {
        SectorRefactor refactor = movingArea.getRefactor();
        boolean started = movingArea.isUserTransactionStarted();
        if (!started)
            movingArea.startUserTransaction();
        try {
            refactor.setSector(sector);
            SectorRefactor.PerspectivePoint point = new SectorRefactor.PerspectivePoint();
            point.type = SectorRefactor.TYPE_END;
            point.setFunction(targetFunction, side);
            Point p = createFunctionPoint(targetFunction, side);
            point.point = p;
            point.x = p.getX();
            point.y = p.getY();
            refactor.setPoint(point);
            boolean changed = refactor.changeSector();
            if (changed) {
                PaintSector.save(sector, new DataLoader.MemoryData(), movingArea
                        .getDataPlugin().getEngine());
                refactor.fixOwners();
                refactor.setUndoPoint();
                movingArea.repaintAsync();
            } else if (!started)
                movingArea.commitUserTransaction();
            return changed;
        } finally {
            if (!started && movingArea.isUserTransactionStarted())
                movingArea.commitUserTransaction();
        }
    }

    private boolean disconnectArrow(PaintSector sector) {
        SectorBorder endBorder = sector.getSector().getEnd();
        if (endBorder == null || endBorder.getFunction() == null
                || !endBorder.getFunction().equals(targetFunction))
            return false;
        int side = endBorder.getFunctionType();
        if (side < 0)
            side = MovingPanel.LEFT;
        SectorRefactor refactor = movingArea.getRefactor();
        boolean started = movingArea.isUserTransactionStarted();
        if (!started)
            movingArea.startUserTransaction();
        try {
            refactor.setSector(sector);
            SectorRefactor.PerspectivePoint point = new SectorRefactor.PerspectivePoint();
            point.type = SectorRefactor.TYPE_END;
            point.borderType = side;
            double[] coords = computeBoundaryCoordinates(side, targetFunction);
            point.x = coords[0];
            point.y = coords[1];
            refactor.setPoint(point);
            boolean changed = refactor.changeSector();
            if (changed) {
                PaintSector.save(sector, new DataLoader.MemoryData(), movingArea
                        .getDataPlugin().getEngine());
                refactor.fixOwners();
                refactor.setUndoPoint();
                movingArea.repaintAsync();
            } else if (!started)
                movingArea.commitUserTransaction();
            return changed;
        } finally {
            if (!started && movingArea.isUserTransactionStarted())
                movingArea.commitUserTransaction();
        }
    }

    private class ArrowEntry {
        private final PaintSector sector;
        private final JCheckBox checkBox;
        private final JComboBox<SideOption> sideCombo;
        private final JPanel panel;

        ArrowEntry(PaintSector sector, String label, boolean connected, int side) {
            this.sector = sector;
            this.panel = new JPanel(new BorderLayout(8, 0));
            this.checkBox = new JCheckBox(label, connected);
            panel.add(checkBox, BorderLayout.CENTER);

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            rightPanel.add(new JLabel(ResourceLoader
                    .getString("ArrowConnections.side")));
            sideCombo = new JComboBox<SideOption>(sideOptions.clone());
            sideCombo.setSelectedItem(findSideOption(side));
            rightPanel.add(sideCombo);
            panel.add(rightPanel, BorderLayout.EAST);

            checkBox.addActionListener(this::toggleConnection);
            sideCombo.addActionListener(this::changeSide);
        }

        JPanel getPanel() {
            panel.setAlignmentX(LEFT_ALIGNMENT);
            return panel;
        }

        private void toggleConnection(ActionEvent event) {
            if (updating)
                return;
            SideOption selected = (SideOption) sideCombo.getSelectedItem();
            if (selected == null) {
                loadArrows();
                return;
            }
            boolean desired = checkBox.isSelected();
            try {
                if (desired) {
                    if (!connectArrow(sector, selected.side))
                        checkBox.setSelected(false);
                } else {
                    if (!disconnectArrow(sector))
                        checkBox.setSelected(true);
                }
            } catch (Exception ex) {
                showError(ResourceLoader
                        .getString("ArrowConnections.error.update"), ex);
            }
            loadArrows();
        }

        private void changeSide(ActionEvent event) {
            if (updating)
                return;
            SideOption selected = (SideOption) sideCombo.getSelectedItem();
            if (selected == null)
                return;
            if (!checkBox.isSelected())
                return;
            try {
                connectArrow(sector, selected.side);
            } catch (Exception ex) {
                showError(ResourceLoader
                        .getString("ArrowConnections.error.update"), ex);
            }
            loadArrows();
        }
    }

    private static class SideOption {
        final int side;
        final String label;

        SideOption(int side) {
            this.side = side;
            this.label = getLabel(side);
        }

        private String getLabel(int side) {
            switch (side) {
                case MovingPanel.LEFT:
                    return ResourceLoader
                            .getString("ArrowConnections.side.left");
                case MovingPanel.RIGHT:
                    return ResourceLoader
                            .getString("ArrowConnections.side.right");
                case MovingPanel.TOP:
                    return ResourceLoader
                            .getString("ArrowConnections.side.top");
                case MovingPanel.BOTTOM:
                    return ResourceLoader
                            .getString("ArrowConnections.side.bottom");
                default:
                    return Integer.toString(side);
            }
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class FunctionChoice {
        final Function function;
        final boolean boundary;
        final String label;

        FunctionChoice(Function function, boolean boundary, String label) {
            this.function = function;
            this.boundary = boundary;
            this.label = label;
        }

        FunctionChoice copy() {
            return new FunctionChoice(function, boundary, label);
        }

        boolean matches(Function function, boolean boundary) {
            if (this.boundary != boundary)
                return false;
            if (this.boundary)
                return true;
            if (function == null)
                return false;
            return this.function != null && this.function.equals(function);
        }

        @Override
        public String toString() {
            return label;
        }
    }
}

package com.ramussoft.gui.elist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeAdapter;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.ElementListPersistent;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ViewTitleEvent;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.elist.ElistPanel.ElementInfo;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.TreeTableNode;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;

public class ElistTabView extends AbstractView implements TabView {

    private static final String USER_GUI_ELEMENT_LIST_VIEW = "/user/gui/element-list/view/";

    private Attribute elementList;

    private ElistPanel left;

    private ElistPanel right;

    private ElistPanel unique;

    private List<ElementListPersistent> value;

    private List<ElementListPersistent> leftValue;

    private List<ElementListPersistent> rightValue;

    private Element element;

    private Element leftElement;

    private Element rightElement;

    private Engine engine;

    private AccessRules rules;

    private boolean revert;

    private boolean readOnly = false;

    private JPanel leftContainer = new JPanel(new BorderLayout());

    private JPanel rightContainer = new JPanel(new BorderLayout());

    private AttributeListener listener = new AttributeAdapter() {
        @Override
        public void attributeUpdated(AttributeEvent event) {
            if (event.getAttribute().equals(elementList)) {
                elementList = (Attribute) event.getNewValue();
                ViewTitleEvent titleEvent = new ViewTitleEvent(
                        ElistTabView.this, event.getAttribute().getName());
                titleChanged(titleEvent);
            }
        }

        @Override
        public void attributeDeleted(AttributeEvent event) {
            if (event.getAttribute().equals(elementList)) {
                close();
            }
        }
    };

    private TableModelListener modelListener = new TableModelListener() {

        public void tableChanged(TableModelEvent e) {
            comp.repaint();
        }

    };

    private ElementAttributeListener leftAttributeListener = new ElementAttributeListener() {
        @Override
        public void attributeChanged(AttributeEvent event) {
            ElistTabView.this.attributeChanged(event);
        }
    };

    private ElementAttributeListener rightAttributeListener = new ElementAttributeListener() {
        @Override
        public void attributeChanged(AttributeEvent event) {
            ElistTabView.this.attributeChanged(event);
        }
    };

    private JPanel component;

    private JPanel comp = new JPanel() {
        /**
         *
         */
        private static final long serialVersionUID = 1691576149229595356L;

        @Override
        public void paint(Graphics gr) {
            super.paint(gr);
            Graphics2D g = (Graphics2D) gr;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.black);
            if (unique != null)
                paint(value, g, isLeftUnique());
            else {
                g.setColor(Color.blue);
                paint(leftValue, g, true);
                g.setColor(Color.red);
                paint(rightValue, g, false);
            }
        }

        private void paint(List<ElementListPersistent> value, Graphics2D g,
                           boolean leftUnique) {
            if ((value == null) || (value.size() == 0))
                return;
            double middle = (getWidth()) / 2d;
            Color color = g.getColor();
            Color light = new Color(light(color.getRed()),
                    light(color.getGreen()), light(color.getBlue()));
            int leftX = 0;
            int rightX = getWidth();

            if (revert) {
                rightX = 0;
                leftX = getWidth();
            }

            if (leftUnique) {
                ElementInfo y = getLeftElementInfo(value.get(0));
                if (y != null) {
                    if (y.collapsed)
                        g.setColor(light);
                    else
                        g.setColor(color);
                    g.draw(new Line2D.Double(leftX, y.y, middle, y.y));
                    for (ElementListPersistent p : value) {
                        ElementInfo ry = getRightElementInfo(p);
                        if (ry != null) {
                            if (ry.collapsed)
                                g.setColor(light);
                            else
                                g.setColor(color);
                            g.draw(new Line2D.Double(middle, y.y, rightX, ry.y));
                        }
                    }
                }
            } else {
                ElementInfo y = getRightElementInfo(value.get(0));
                if (y != null) {
                    if (y.collapsed)
                        g.setColor(light);
                    else
                        g.setColor(color);
                    g.draw(new Line2D.Double(rightX, y.y, middle, y.y));
                    for (ElementListPersistent p : value) {
                        ElementInfo ly = getLeftElementInfo(p);
                        if (ly != null) {
                            if (ly.collapsed)
                                g.setColor(light);
                            else
                                g.setColor(color);
                            g.draw(new Line2D.Double(leftX, ly.y, middle, y.y));
                        }
                    }
                }
            }
        }

        private int light(int red) {
            return 255 - (255 - red) / 4;
        }

        private ElementInfo getRightElementInfo(
                ElementListPersistent elementListPersistent) {
            return right.getElementInfo(elementListPersistent.getElement2Id());
        }

        private ElementInfo getLeftElementInfo(
                ElementListPersistent elementListPersistent) {
            return left.getElementInfo(elementListPersistent.getElement1Id());
        }
    };

    private Qualifier q1;

    private Qualifier q2;

    private AbstractAction revertAction = new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = -3460703953840412148L;

        {
            putValue(ACTION_COMMAND_KEY, "Action.ElementList.Revert");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/revert.png")));
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            revert = (Boolean) getValue(SELECTED_KEY);
            leftContainer.removeAll();
            rightContainer.removeAll();
            reAddPanels();
            leftContainer.revalidate();
            rightContainer.revalidate();
            component.repaint();
        }
    };

    private JComponent rightComponent;

    private JComponent leftComponent;

    public ElistTabView(GUIFramework framework, Attribute elementList) {
        super(framework);
        this.elementList = elementList;
        this.engine = framework.getEngine();
        this.rules = framework.getAccessRules();

        engine.addAttributeListener(listener);
    }

    @SuppressWarnings("unchecked")
    protected void attributeChanged(AttributeEvent event) {
        if (elementList.equals(event.getAttribute())) {
            if (event.isJournaled()) {
                if (readOnly)
                    return;
                Element e = event.getElement();
                ElistPanel panel = (e.getQualifierId() == q1.getId()) ? left
                        : right;
                ElistPanel opposite = getOpposite(panel);
                opposite.clearSelection();
                panel.clearSelection();
                panel.setSelectType(SelectType.RADIO);

                List<Long> list = new ArrayList<Long>(1);
                list.add(e.getId());
                panel.getComponent().getModel().selectRows(list);

                unique = panel;
                opposite.setSelectType(SelectType.CHECK);
                value = (List<ElementListPersistent>) event.getNewValue();
                list = getSelected(opposite);
                opposite.getComponent().getModel().selectRows(list);
                leftValue = null;
                rightValue = null;
                comp.repaint();
                left.getComponent().repaint();
                right.getComponent().repaint();
            } else {
                if ((leftValue != null)
                        && (leftElement.equals(event.getElement()))) {
                    leftValue = (List<ElementListPersistent>) event
                            .getNewValue();
                    comp.repaint();
                }
                if ((rightValue != null)
                        && (rightElement.equals(event.getElement()))) {
                    rightValue = (List<ElementListPersistent>) event
                            .getNewValue();
                    comp.repaint();
                }
                if ((unique != null) && (element != null)
                        && (element.equals(event.getElement()))) {
                    value = (List<ElementListPersistent>) event.getNewValue();
                    ElistPanel opposite = getOpposite(unique);
                    List<Long> sels = getSelected(opposite);
                    opposite.getComponent().getModel().selectRows(sels);
                    comp.repaint();
                }
            }
        }
    }

    protected boolean isLeftUnique() {
        return left == unique;
    }

    @Override
    public JComponent createComponent() {
        final Engine engine = framework.getEngine();

        AccessRules rules = framework.getAccessRules();
        ElementListPropertyPersistent p = (ElementListPropertyPersistent) engine
                .getAttribute(null, elementList);
        boolean addListener = q1 == null;
        q1 = engine.getQualifier(p.getQualifier1());
        q2 = engine.getQualifier(p.getQualifier2());

        readOnly = !((rules.canUpdateAttribute(q1.getId(), elementList.getId())) && (rules
                .canUpdateAttribute(q2.getId(), elementList.getId())));

        if (addListener) {
            engine.addElementAttributeListener(q1, leftAttributeListener);
            engine.addElementAttributeListener(q2, rightAttributeListener);
        }

        left = new ElistPanel(framework, engine, rules, q1, elementList) {
            @Override
            public String getPropertiesPrefix() {
                return "left" + elementList.getId();
            }

            @Override
            public void changeSelection(SelectionEvent event) {
                ElistTabView.this.changeSelection(this, event);

            }

            @Override
            public JComponent createComponent() {
                unique = null;
                if (getComponent() != null) {
                    left.clearSelection();
                    left.setSelectType(SelectType.RADIO);
                    right.clearSelection();
                    right.setSelectType(SelectType.RADIO);
                }
                JComponent component2 = super.createComponent();

                getComponent().getPane().getVerticalScrollBar()
                        .addAdjustmentListener(new AdjustmentListener() {
                            @Override
                            public void adjustmentValueChanged(AdjustmentEvent e) {
                                comp.repaint();
                            }
                        });

                getComponent().getTable().getModel()
                        .addTableModelListener(modelListener);

                getComponent().getTable().getSelectionModel()
                        .addListSelectionListener(new ListSelectionListener() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public void valueChanged(ListSelectionEvent e) {
                                TreeTableNode node = left.getComponent()
                                        .getTable().getSelectedNode();
                                if ((node != null) && (node.getRow() != null)) {
                                    leftElement = node.getRow().getElement();
                                    leftValue = (List<ElementListPersistent>) engine
                                            .getAttribute(leftElement,
                                                    elementList);
                                    if (unique == null)
                                        rightValue = null;
                                } else
                                    leftValue = null;
                                comp.repaint();
                            }
                        });

                return component2;
            }

            @Override
            public void setSelectType(SelectType selectType) {
                if ((!readOnly) || (selectType.equals(SelectType.NONE)))
                    super.setSelectType(selectType);
            }

        };
        right = new ElistPanel(framework, engine, rules, q2, elementList) {
            @Override
            public String getPropertiesPrefix() {
                return "right" + elementList.getId();
            }

            @Override
            public void changeSelection(SelectionEvent event) {
                ElistTabView.this.changeSelection(this, event);

            }

            @Override
            public JComponent createComponent() {
                unique = null;
                if (getComponent() != null) {
                    left.clearSelection();
                    left.setSelectType(SelectType.RADIO);
                    right.clearSelection();
                    right.setSelectType(SelectType.RADIO);
                }
                JComponent component2 = super.createComponent();
                getComponent().getPane().getVerticalScrollBar()
                        .addAdjustmentListener(new AdjustmentListener() {
                            @Override
                            public void adjustmentValueChanged(AdjustmentEvent e) {
                                comp.repaint();
                            }
                        });
                getComponent().getTable().getModel()
                        .addTableModelListener(modelListener);

                getComponent().getTable().getSelectionModel()
                        .addListSelectionListener(new ListSelectionListener() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public void valueChanged(ListSelectionEvent e) {
                                TreeTableNode node = right.getComponent()
                                        .getTable().getSelectedNode();
                                if ((node != null) && (node.getRow() != null)) {
                                    rightElement = node.getRow().getElement();
                                    rightValue = (List<ElementListPersistent>) engine
                                            .getAttribute(rightElement,
                                                    elementList);
                                    if (unique == null)
                                        leftValue = null;
                                } else
                                    rightValue = null;
                                comp.repaint();
                            }
                        });
                return component2;
            }

            @Override
            public void setSelectType(SelectType selectType) {
                if ((!readOnly) || (selectType.equals(SelectType.NONE)))
                    super.setSelectType(selectType);
            }
        };

        JSplitPane pane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane1.setRightComponent(rightContainer);
        comp.setBackground(Color.white);
        pane1.setLeftComponent(comp);

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setLeftComponent(leftContainer);

        Properties properties = engine.getProperties(getProperiesName());

        revert = Options.getBoolean("REVERT", false, properties);

        revertAction.putValue(Action.SELECTED_KEY, revert);

        addPanels();

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(pane1, BorderLayout.CENTER);

        pane.setRightComponent(panel);

        component = new JPanel(new BorderLayout());

        component.add(pane, BorderLayout.CENTER);

        Options.loadOptions(component, properties);

        if (readOnly) {
            left.setSelectType(SelectType.NONE);
            right.setSelectType(SelectType.NONE);
        }

        return component;
    }

    private void addPanels() {
        rightComponent = right.createComponent();
        leftComponent = left.createComponent();
        reAddPanels();
    }

    @SuppressWarnings("unchecked")
    private void changeSelection(ElistPanel elistPanel, SelectionEvent event) {
        ElistPanel opposite = getOpposite(elistPanel);
        element = event.getRows()[0].getElement();

        if (unique == null) {
            unique = elistPanel;
            opposite.setSelectType(SelectType.CHECK);
            value = (List<ElementListPersistent>) engine.getAttribute(
                    event.getRows()[0].getElement(), elementList);
            List<Long> list = getSelected(opposite);
            opposite.getComponent().getModel().selectRows(list);
        } else if (unique == elistPanel) {
            opposite.clearSelection();
            if (elistPanel.getComponent().getModel().getSelectedRowCount() == 0) {
                unique = null;
                opposite.setSelectType(SelectType.RADIO);
            } else {
                value = (List<ElementListPersistent>) engine.getAttribute(
                        event.getRows()[0].getElement(), elementList);
                List<Long> list = getSelected(opposite);
                opposite.getComponent().getModel().selectRows(list);
            }
        } else {
            Element element = unique.getSelectedRows().get(0).getElement();
            if (rules.canUpdateElement(element.getId(), elementList.getId())) {
                for (Row row : event.getRows()) {
                    if (event.isSelected())
                        addRow(elistPanel, row);
                    else
                        removeRow(elistPanel, row);
                }
                ((Journaled) engine).startUserTransaction();
                engine.setAttribute(element, elementList, value);
                ((Journaled) engine).commitUserTransaction();
            } else {
                List<Long> list = getSelected(elistPanel);
                elistPanel.getComponent().getModel().selectRows(list);
            }
        }
        comp.repaint();
    }

    private void removeRow(ElistPanel elistPanel, Row row) {
        if (left == elistPanel) {
            for (ElementListPersistent p : value) {
                if (p.getElement1Id() == row.getElementId()) {
                    value.remove(p);
                    break;
                }
            }
        } else {
            for (ElementListPersistent p : value) {
                if (p.getElement2Id() == row.getElementId()) {
                    value.remove(p);
                    break;
                }
            }
        }
    }

    private void addRow(ElistPanel elistPanel, Row row) {
        ElementListPersistent p = new ElementListPersistent();
        if (left == elistPanel) {
            p.setElement1Id(row.getElementId());
            p.setElement2Id(right.getSelectedRows().get(0).getElementId());
        } else {
            p.setElement1Id(left.getSelectedRows().get(0).getElementId());
            p.setElement2Id(row.getElementId());
        }
        if (value.indexOf(p) < 0)
            value.add(p);
    }

    private List<Long> getSelected(ElistPanel panel) {
        List<Long> res = new ArrayList<Long>(value.size());
        if (panel == right)
            for (ElementListPersistent p : value)
                res.add(p.getElement2Id());
        else
            for (ElementListPersistent p : value)
                res.add(p.getElement1Id());
        return res;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{revertAction};
    }

    protected void reAddPanels() {
        if (revert) {
            leftContainer.add(rightComponent, BorderLayout.CENTER);
            rightContainer.add(leftComponent, BorderLayout.CENTER);
        } else {
            leftContainer.add(leftComponent, BorderLayout.CENTER);
            rightContainer.add(rightComponent, BorderLayout.CENTER);
        }
    }

    @Override
    public String getTitle() {
        return elementList.getName();
    }

    @Override
    public void close() {
        Engine engine = framework.getEngine();
        super.close();
        Properties properties = new Properties();
        Options.setBoolean("REVERT", revert, properties);
        Options.saveOptions(component, properties);
        engine.setProperties(getProperiesName(), properties);
        left.close();
        right.close();
        engine.removeAttributeListener(listener);
        engine.removeElementAttributeListener(q1, leftAttributeListener);
        engine.removeElementAttributeListener(q2, rightAttributeListener);
    }

    @Override
    public ActionEvent getOpenAction() {
        return new ActionEvent(ElistPlugin.OPEN_ELEMENT_LIST, elementList);
    }

    private String getProperiesName() {
        return USER_GUI_ELEMENT_LIST_VIEW + getClass().getName()
                + elementList.getId() + ".xml";
    }

    private ElistPanel getOpposite(ElistPanel panel) {
        if (panel == left)
            return right;
        return left;
    }

}

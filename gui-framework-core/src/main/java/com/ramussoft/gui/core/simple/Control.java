package com.ramussoft.gui.core.simple;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.EventListenerList;

import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.core.PlugableFrame;

public class Control {

    private ContentArea contentArea;

    private List<String> layouts = new ArrayList<String>();

    private List<UniqueDFrame> uniqueFrames = new ArrayList<UniqueDFrame>();

    private Hashtable<String, UniqueViewsHolder> data = new Hashtable<String, UniqueViewsHolder>();

    private UniqueViewsHolder current;

    private Area area;

    private PlugableFrame frame;

    private SimleGUIPluginFactory factory;

    private String layout;

    private EventListenerList listenerList = new EventListenerList();

    private List<DFrame> frames = new ArrayList<DFrame>();

    private DFrame active;

    public Control(PlugableFrame plugableFrame, SimleGUIPluginFactory factory) {
        this.contentArea = new ContentArea();
        this.frame = plugableFrame;
        this.factory = factory;
    }

    public void save(String layout) {
        Options.saveOptions("layout." + layout, frame);
    }

    public DFrame getActiveFrame() {
        return active;
    }

    public List<DFrame> getFrames() {
        return frames;
    }

    public void addDFrame(DFrame frame) {
        frames.add(frame);
    }

    public void setActive(DFrame dockable) {
        if (dockable instanceof TabFrame) {
            openTab(dockable, area);
        } else {
            UniqueDFrame frame = (UniqueDFrame) dockable;
            UniqueView view = (UniqueView) frame.getView();
            if (!view.getDefaultWorkspace().equals(layout))
                factory.setCurrentWorkspace(getDefaultWorkspaces(
                        view.getDefaultWorkspace()).get(0));
            UniqueViewsHolder holder = getHolder(layout);
            if (holder.left instanceof JTabbedPane)
                openTab(dockable, (JTabbedPane) holder.left);
            if (holder.top instanceof JTabbedPane)
                openTab(dockable, (JTabbedPane) holder.top);

            if (holder.right instanceof JTabbedPane)
                openTab(dockable, (JTabbedPane) holder.right);

            if (holder.bottom instanceof JTabbedPane)
                openTab(dockable, (JTabbedPane) holder.bottom);
        }
        dockable.requestFocus();
    }

    private void openTab(Component component, JTabbedPane pane) {
        for (int i = 0; i < pane.getTabCount(); i++) {
            if (component == pane.getComponentAt(i)) {
                pane.setSelectedIndex(i);
                break;
            }
        }
    }

    public void addControlListener(ControlListener controlListener) {
        listenerList.add(ControlListener.class, controlListener);
    }

    public ContentArea getContentArea() {
        return contentArea;
    }

    public void add(UniqueDFrame dockable) {
        uniqueFrames.add(dockable);
    }

    public String[] layouts() {
        return layouts.toArray(new String[layouts.size()]);
    }

    public void load(String layout) {
        this.layout = layout;
        if (layouts.indexOf(layout) < 0)
            return;
        if (current != null) {
            if (current.mainArea == null) {
                contentArea.remove(area);
            } else {
                contentArea.remove(current.mainArea);
                if (current.pane != null)
                    current.pane.remove(area);
            }
            for (UniqueDFrame f : current.toRemove)
                current.wrappers.get(f).remove(f);
        }

        current = getHolder(layout);
        for (UniqueDFrame f : current.toRemove)
            current.wrappers.get(f).add(f);

        if (current.mainArea == null) {
            contentArea.add(area, BorderLayout.CENTER);
        } else {
            contentArea.add(current.mainArea, BorderLayout.CENTER);
            if (current.pane != null)
                if (current.clientLeft)
                    current.pane.setLeftComponent(area);
                else
                    current.pane.setRightComponent(area);
        }

        String name = "layout." + layout;
        Properties properties = Options.getProperties(name);

        Options.loadOptions(name, frame, properties, false);

        contentArea.revalidate();
        contentArea.repaint();
    }

    public void addFocusListener(FrameFocusListener frameFocusListener) {
        listenerList.add(FrameFocusListener.class, frameFocusListener);
    }

    public Area createWorkingArea(String id) {
        area = new Area(id);
        contentArea.add(area, BorderLayout.CENTER);
        area.addCloseableTabbedPaneListener(new CloseableTabbedPaneListener() {

            @Override
            public boolean closeTab(int tabIndexToClose) {
                DFrame dockable = (DFrame) area.getComponentAt(tabIndexToClose);
                frames.remove(dockable);
                if (active == dockable) {
                    focusLost(active);
                }
                for (ControlListener l : listenerList
                        .getListeners(ControlListener.class)) {
                    l.closed(Control.this, dockable);
                }
                return true;
            }
        });
        return area;
    }

    public void commitUniqueViews() {
        for (UniqueDFrame dFrame : uniqueFrames) {
            UniqueView view = (UniqueView) dFrame.getView();
            List<String> wks = getDefaultWorkspaces(view.getDefaultWorkspace());
            for (String layout : wks) {
                if (layouts.indexOf(layout) < 0)
                    layouts.add(layout);
                UniqueViewsHolder holder = getHolder(layout);
                if (BorderLayout.EAST.equals(view.getDefaultPosition()))
                    holder.rights.add(dFrame);
                else if (BorderLayout.SOUTH.equals(view.getDefaultPosition()))
                    holder.bottoms.add(dFrame);
                else if (BorderLayout.NORTH.equals(view.getDefaultPosition()))
                    holder.tops.add(dFrame);
                else
                    holder.lefts.add(dFrame);

                if (wks.size() > 1)
                    holder.toRemove.add(dFrame);
            }
        }
        for (String layout : layouts) {
            getHolder(layout).createComponents();
        }
    }

    private List<String> getDefaultWorkspaces(String string) {
        StringTokenizer st = new StringTokenizer(string, "|");
        List<String> list = new ArrayList<String>(1);
        while (st.hasMoreTokens())
            list.add(st.nextToken());
        return list;
    }

    private UniqueViewsHolder getHolder(String layout) {
        UniqueViewsHolder holder = data.get(layout);
        if (holder == null) {
            holder = new UniqueViewsHolder();
            data.put(layout, holder);
        }
        return holder;
    }

    private class UniqueViewsHolder {
        private JComponent left;

        private JComponent right;

        private JComponent bottom;

        private JComponent top;

        private List<UniqueDFrame> tops = new ArrayList<UniqueDFrame>();

        private List<UniqueDFrame> lefts = new ArrayList<UniqueDFrame>();

        private List<UniqueDFrame> rights = new ArrayList<UniqueDFrame>();

        private List<UniqueDFrame> bottoms = new ArrayList<UniqueDFrame>();

        private List<UniqueDFrame> toRemove = new ArrayList<UniqueDFrame>();

        private JComponent mainArea;

        private JSplitPane pane;

        private boolean clientLeft;

        private Hashtable<UniqueDFrame, JComponent> wrappers = new Hashtable<UniqueDFrame, JComponent>();

        public void createComponents() {
            left = createComponents(lefts);
            top = createComponents(tops);
            right = createComponents(rights);
            bottom = createComponents(bottoms);

            if (mainArea == null)
                if (left == null)
                    mainArea = null;
                else if (right != null) {
                    if ((top == null) || (bottom == null)) {
                        JSplitPane pane = new JSplitPane();
                        pane.setOneTouchExpandable(true);
                        this.pane = new JSplitPane();
                        this.pane.setOneTouchExpandable(true);
                        this.mainArea = pane;
                        pane.setRightComponent(this.pane);
                        pane.setLeftComponent(left);
                        this.pane.setRightComponent(right);
                        this.pane.setResizeWeight(0.8);
                        clientLeft = true;
                    } else {
                        JSplitPane b = new JSplitPane();
                        this.mainArea = b;
                        b.setOrientation(JSplitPane.VERTICAL_SPLIT);
                        b.setLeftComponent(top);
                        JSplitPane pane = new JSplitPane();
                        b.setRightComponent(pane);

                        JSplitPane pane2 = new JSplitPane();
                        pane.setLeftComponent(left);
                        pane.setRightComponent(pane2);
                        JSplitPane pane3 = new JSplitPane();
                        pane3.setOrientation(JSplitPane.VERTICAL_SPLIT);

                        pane2.setLeftComponent(pane3);
                        pane2.setRightComponent(right);

                        this.pane = pane3;
                        pane3.setOneTouchExpandable(true);
                        pane2.setOneTouchExpandable(true);
                        pane.setOneTouchExpandable(true);
                        b.setOneTouchExpandable(true);
                        pane3.setRightComponent(bottom);

                        pane2.setResizeWeight(0.8);
                        pane3.setResizeWeight(0.8);

                        clientLeft = true;

                    }
                } else if (bottom != null) {
                    JSplitPane pane = new JSplitPane();
                    pane.setOneTouchExpandable(true);
                    this.pane = new JSplitPane();
                    this.pane.setOneTouchExpandable(true);
                    this.pane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                    this.mainArea = pane;

                    this.pane.setResizeWeight(0.8);
                    pane.setRightComponent(this.pane);
                    pane.setLeftComponent(left);
                    this.pane.setRightComponent(bottom);
                    clientLeft = true;
                } else {// only left
                    pane = new JSplitPane();
                    pane.setOneTouchExpandable(true);
                    this.pane.setResizeWeight(0.2);
                    mainArea = pane;
                    pane.setLeftComponent(left);
                    clientLeft = false;
                }
        }

        private JComponent createComponents(List<UniqueDFrame> list) {
            if (list.size() > 0) {
                UniqueDFrame frame = list.get(0);
                if (BorderLayout.CENTER.equals(((UniqueView) frame.getView())
                        .getDefaultPosition())) {
                    mainArea = frame;
                    return null;
                }
                JTabbedPane pane = new JTabbedPane();
                for (UniqueDFrame dFrame : list) {
                    if (toRemove.indexOf(dFrame) >= 0) {
                        JPanel panel = new JPanel(new BorderLayout());
                        pane.addTab(dFrame.getTitleText(), panel);
                        wrappers.put(dFrame, panel);
                    } else
                        pane.addTab(dFrame.getTitleText(), dFrame);

                }

                return pane;
            }
            return null;
        }
    }

    public void focusLost(DFrame dFrame) {
        for (FrameFocusListener listener : listenerList
                .getListeners(FrameFocusListener.class))
            listener.focusLost(dFrame);
        active = null;
    }

    public void focusGained(DFrame dFrame) {
        if (active != null)
            focusLost(active);

        for (FrameFocusListener listener : listenerList
                .getListeners(FrameFocusListener.class))
            listener.focusGained(dFrame);
        active = dFrame;
    }

    public void closeTab(TabFrame dockable) {
        area.closeTab(dockable);
    }

}

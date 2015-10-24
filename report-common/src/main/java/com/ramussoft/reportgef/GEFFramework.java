package com.ramussoft.reportgef;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Bounds;

public class GEFFramework {

    protected Engine engine;

    protected AccessRules accessRules;

    protected Hashtable<String, ComponentFactory> componentFactories = new Hashtable<String, ComponentFactory>();

    protected GEFFramework(GUIFramework framework) {
        if (framework != null) {
            this.engine = framework.getEngine();
            this.accessRules = framework.getAccessRules();
            loadFactories();
        }
    }

    protected void loadFactories() {
        throw new RuntimeException("Method was disable in ramus report gef package");
    }

    public Component getComponent(Bounds bounds) {
        String type = bounds.getComponentType();
        return componentFactories.get(type).getComponent(engine, accessRules,
                bounds);
    }

    public Component createComponent(Diagram diagram, Bounds bounds) {
        String type = bounds.getComponentType();
        return componentFactories.get(type).createComponent(diagram,
                engine, accessRules, bounds);
    }

    public static GEFFramework getFramework(GUIFramework framework) {
        GEFFramework framework2 = (GEFFramework) framework.get("GEF_FRAMEFORK");
        if (framework2 == null) {
            framework2 = new GEFFramework(framework);
            framework.put("GEF_FRAMEFORK", framework2);
        }
        return framework2;
    }

    public Engine getEngine() {
        return engine;
    }

    public List<ComponentFactory> getComponentFactories(String type) {
        ArrayList<ComponentFactory> factories = new ArrayList<ComponentFactory>();
        for (ComponentFactory factory : componentFactories.values()) {
            if ((factory.getType() == null) || (type == null)
                    || (factory.getType().equals(type)))
                factories.add(factory);
        }
        return factories;
    }

    public AccessRules getAccessRules() {
        return accessRules;
    }
}

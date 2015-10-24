package com.ramussoft.idef0;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipException;

import com.dsoft.pb.idef.elements.ProjectOptions;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.database.common.ElementCreationCallback;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.data.negine.NDataPlugin;
import com.ramussoft.pb.data.negine.NFunction;

public class NDataPluginFactory {

    private static DataPlugin templateDataPlugin = null;

    private static final Object lock = new Object();

    private Qualifier baseFunction;

    private Row rowBaseFunction = null;

    private NDataPlugin dataPlugin;

    private DataPlugin createDataPlugin() {

        DataPlugin plugin = (DataPlugin) Proxy.newProxyInstance(getClass()
                        .getClassLoader(), new Class[]{DataPlugin.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method,
                                         Object[] args) throws Throwable {
                        try {

                            String name = method.getName();

                            if ("getBaseFunction".equals(name)) {
                                if (rowBaseFunction == null) {
                                    rowBaseFunction = getBaseFunction(
                                            dataPlugin, baseFunction);
                                }
                                return rowBaseFunction;
                            }

                            if ("getBaseFunctionQualifier".equals(name))
                                return baseFunction;

                            if ("isReadOnly".equals(name))
                                return !dataPlugin.getAccessRules()
                                        .canUpdateQualifier(
                                                baseFunction.getId());

                            if ("createRow".equals(name)) {
                                com.ramussoft.pb.Row parent = (com.ramussoft.pb.Row) args[0];
                                RowSet set = dataPlugin.getRowSet(baseFunction
                                        .getId());
                                if (parent instanceof Function) {
                                    Row row = set.createRow((Row) parent);
                                    ((NFunction) row).setDefaultValues();
                                    ((NFunction) row)
                                            .setDecompositionType(((Function) parent)
                                                    .getDecompositionType());
                                    return row;
                                }
                            } else if ("createFunction".equals(name)) {
                                com.ramussoft.pb.Function parent = (com.ramussoft.pb.Function) args[0];
                                final Integer type = (Integer) args[1];
                                RowSet set = dataPlugin.getRowSet(baseFunction
                                        .getId());
                                if (parent instanceof Function) {
                                    Row row = set.createRow((Row) parent,
                                            new ElementCreationCallback() {

                                                @Override
                                                public void created(
                                                        Element element) {
                                                    Engine engine = dataPlugin
                                                            .getEngine();
                                                    Attribute attribute = IDEF0Plugin
                                                            .getFunctionTypeAttribute(engine);
                                                    engine.setAttribute(
                                                            element, attribute,
                                                            type);
                                                }
                                            });
                                    ((NFunction) row).setDefaultValues();
                                    ((NFunction) row)
                                            .setDecompositionType(((Function) parent)
                                                    .getDecompositionType());
                                    return row;
                                }
                            } else if ((("getChilds".equals(name)) || ("getRecChilds"
                                    .equals(name)))
                                    && (args[0] == null)
                                    && (((Boolean) args[1]) == false)) {
                                Vector v = (Vector) method.invoke(dataPlugin,
                                        args);
                                v.add(0, dataPlugin.getBaseStream());
                                v.add(0,
                                        getBaseFunction(dataPlugin,
                                                baseFunction));
                                return v;
                            }
                            if ("getProjectOptions".equals(name))
                                return getProjectOptions();

                            if ("setProjectOptions".equals(name))
                                return setProjectOptions((ProjectOptions) args[0]);

                            if ("refresh".equals(name)) {
                                fullRefrash((GUIFramework) args[0]);

                                return null;
                            }

                            return method.invoke(dataPlugin, args);
                        } catch (InvocationTargetException e) {
                            throw e.getTargetException();
                        }
                    }

                    private Row getBaseFunction(final NDataPlugin dataPlugin,
                                                final Qualifier baseFunction) {
                        return dataPlugin.getNBaseFunction(baseFunction.getId());
                    }

                    protected Object setProjectOptions(
                            ProjectOptions projectOptions) {
                        getBaseFunction(dataPlugin, baseFunction)
                                .setAttribute(
                                        IDEF0Plugin
                                                .getProjectPreferencesAttrtibute(dataPlugin
                                                        .getEngine()),
                                        projectOptions);
                        return null;
                    }

                    protected Object getProjectOptions() {
                        return getBaseFunction(dataPlugin, baseFunction)
                                .getAttribute(
                                        IDEF0Plugin
                                                .getProjectPreferencesAttrtibute(dataPlugin
                                                        .getEngine()));
                    }
                });
        plugin.getBaseFunction();
        return plugin;
    }

    public static DataPlugin getDataPlugin(Qualifier baseFunction, Engine e,
                                           AccessRules accessRules) {
        NDataPlugin dataPlugin = getExistingDataPlugin(e);
        if (dataPlugin == null) {
            dataPlugin = new NDataPlugin(e, accessRules);
            e.setPluginProperty("IDEF0", "DataPlugin", dataPlugin);
        }

        if (baseFunction == null)
            return dataPlugin;

        DataPlugin dp = (DataPlugin) e.getPluginProperty("IDEF0",
                "BaseFunction_" + baseFunction.getId());
        if (dp == null) {
            NDataPluginFactory dataPluginFactory = new NDataPluginFactory(e,
                    baseFunction, dataPlugin);

            List<NDataPluginFactory> list = (List<NDataPluginFactory>) e
                    .getPluginProperty("IDEF0", "NDataPluginFactoryList");

            if (list == null) {
                list = new ArrayList<NDataPluginFactory>();
                e.setPluginProperty("IDEF0", "NDataPluginFactoryList", list);
                e.setPluginProperty("IDEF0", "NDataPluginRefreshId", new Long(
                        -1l));
            }

            list.add(dataPluginFactory);

            dp = dataPluginFactory.createDataPlugin();
            e.setPluginProperty("IDEF0",
                    "BaseFunction_" + baseFunction.getId(), dp);
        }
        initTemplate();
        return dp;
    }

    public static NDataPlugin getExistingDataPlugin(Engine e) {
        return (NDataPlugin) e.getPluginProperty("IDEF0", "DataPlugin");
    }

    public static DataPlugin getTemplateDataPlugin() {
        DataPlugin res = null;
        synchronized (lock) {
            if (templateDataPlugin != null) {
                res = templateDataPlugin;
                templateDataPlugin = null;
            } else {
                res = createTemplateDataPlugin();
            }
        }
        initTemplate();
        return res;
    }

    private static DataPlugin createTemplateDataPlugin() {
        MemoryDatabase database = new MemoryDatabase() {
            @Override
            protected Collection<? extends PluginProvider> getAdditionalSuits() {
                ArrayList<PluginProvider> ps = new ArrayList<PluginProvider>(1);
                ps.add(new IDEF0PluginProvider());
                return ps;
            }

            @Override
            protected String getJournalDirectoryName(String tmp) {
                return null;
            }

            @Override
            protected FileIEngineImpl createFileIEngine(PluginFactory factory)
                    throws ClassNotFoundException, ZipException, IOException {
                return createNotSessionedFileIEngine(factory);
            }
        };

        Engine e = database.getEngine(null);
        AccessRules rules = database.getAccessRules(null);
        Qualifier q = e.createQualifier();
        IDEF0Plugin.installFunctionAttributes(q, e);
        return getDataPlugin(q, e, rules);
    }

    private NDataPluginFactory(Engine e, Qualifier aBaseFunction,
                               NDataPlugin dataPlugin) {
        e.addQualifierListener(new QualifierAdapter() {
            @Override
            public void qualifierUpdated(QualifierEvent event) {
                if (event.getNewQualifier().equals(baseFunction)) {
                    baseFunction = event.getNewQualifier();
                }
            }
        });
        this.baseFunction = aBaseFunction;
        this.dataPlugin = dataPlugin;
    }

    public static void fullRefrash(GUIFramework framework) {

        Engine engine = framework.getEngine();

        Long long1 = (Long) engine.getPluginProperty("IDEF0",
                "NDataPluginRefreshId");
        if (long1 == null)
            return;

        long id = framework.getActionId(Commands.FULL_REFRESH);
        if (id == long1.longValue())
            return;

        engine.setPluginProperty("IDEF0", "NDataPluginRefreshId", new Long(id));
        List<NDataPluginFactory> list = (List<NDataPluginFactory>) engine
                .getPluginProperty("IDEF0", "NDataPluginFactoryList");

        NDataPlugin dataPlugin = getExistingDataPlugin(engine);

        dataPlugin.close();
        dataPlugin = new NDataPlugin(dataPlugin.getEngine(),
                dataPlugin.getAccessRules());
        for (NDataPluginFactory f : list) {
            f.dataPlugin = dataPlugin;
            f.rowBaseFunction = null;
        }
        engine.setPluginProperty("IDEF0", "DataPlugin", dataPlugin);
    }

    private static void initTemplate() {
        Thread thread = null;
        synchronized (lock) {
            if (templateDataPlugin == null) {
                thread = new Thread() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            if (templateDataPlugin == null) {
                                templateDataPlugin = createTemplateDataPlugin();
                            }
                        }
                    }
                };
                thread.setPriority(Thread.MIN_PRIORITY);
            }
        }
        if (thread != null)
            thread.start();
    }

}

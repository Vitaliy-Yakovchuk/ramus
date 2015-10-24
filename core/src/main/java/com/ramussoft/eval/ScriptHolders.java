package com.ramussoft.eval;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.event.StreamAdapter;
import com.ramussoft.common.event.StreamEvent;
import com.ramussoft.eval.event.FunctionsChangeEvent;
import com.ramussoft.eval.event.FunctionsChangeListener;

public class ScriptHolders {

    private Hashtable<String, ScriptFunctionsHolder> hashtable = new Hashtable<String, ScriptFunctionsHolder>();

    private List<FunctionsChangeListener> listeners = new ArrayList<FunctionsChangeListener>(
            1);

    public ScriptHolders(final Engine engine) {
        Util util = Util.getUtils(engine);
        if (!util.isDisableScripts()) {
            for (String s : engine.getStreamNames()) {
                if (s.startsWith("/script/")) {
                    byte[] bs = engine.getStream(s);
                    try {
                        ScriptFunctionsHolder holder = new ScriptFunctionsHolder(
                                new String(bs, "UTF-8"));
                        hashtable.put(s, holder);
                    } catch (Exception e) {
                        if (Metadata.DEBUG)
                            e.printStackTrace();
                    }
                }
            }
            engine.addStreamListener(new StreamAdapter() {
                @Override
                public void streamDeleted(StreamEvent event) {
                    String s = event.getPath();
                    if (s.startsWith("/script/")) {
                        hashtable.remove(s);
                    }
                }

                @Override
                public void streamUpdated(StreamEvent event) {
                    String s = event.getPath();
                    if (s.startsWith("/script/")) {
                        byte[] bs = engine.getStream(s);
                        try {
                            ScriptFunctionsHolder holder = new ScriptFunctionsHolder(
                                    new String(bs, "UTF-8"));
                            hashtable.put(s, holder);

                            FunctionsChangeEvent event2 = new FunctionsChangeEvent(
                                    holder.getFunctions());
                            for (FunctionsChangeListener listener : listeners)
                                listener.functionsChanged(event2);

                        } catch (Exception e) {
                            if (Metadata.DEBUG)
                                e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public EObject tryInvoke(String functionName, EObject[] objects2) {
        Object[] objects = new Object[objects2.length];
        for (int i = 0; i < objects2.length; i++) {
            objects[i] = objects2[i];
        }
        for (ScriptFunctionsHolder holder : hashtable.values()) {
            EObject object = holder.tryToInvoke(functionName, objects);
            if (object != null)
                return object;
        }
        return new EObject(null);
    }

    public boolean isFunctionExists(String function) {
        for (ScriptFunctionsHolder holder : hashtable.values()) {
            if (holder.isFunctionExists(function))
                return true;
        }
        return false;
    }

    public void addFunctionsChangeListener(FunctionsChangeListener listener) {
        listeners.add(listener);
    }

    public void removeFunctionsChangeListener(FunctionsChangeListener listener) {
        listeners.remove(listener);
    }

    public FunctionsChangeListener[] getFunctionsChangeListeners() {
        return listeners.toArray(new FunctionsChangeListener[listeners.size()]);
    }
}

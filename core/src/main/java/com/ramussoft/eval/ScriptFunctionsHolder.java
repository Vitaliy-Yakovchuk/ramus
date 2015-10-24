package com.ramussoft.eval;

import java.util.Set;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptFunctionsHolder {

    private ScriptEngine engine;

    public ScriptFunctionsHolder(String script) throws ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        engine = factory.getEngineByName("JavaScript");
        engine.eval(script);
    }

    public EObject tryToInvoke(String functionName, Object[] objects) {
        try {
            if (engine.get(functionName) == null)
                return null;
            Object res = ((Invocable) engine).invokeFunction(functionName,
                    objects);
            return new EObject(res);
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isFunctionExists(String function) {
        if (engine.get(function) != null)
            return true;
        return false;
    }

    public String[] getFunctions() {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        Set<String> list = bindings.keySet();
        return list.toArray(new String[list.size()]);
    }
}

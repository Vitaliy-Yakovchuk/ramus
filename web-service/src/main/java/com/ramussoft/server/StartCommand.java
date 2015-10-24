package com.ramussoft.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.ramussoft.common.Engine;
import com.ramussoft.common.cached.CachedEngine;
import com.ramussoft.common.journal.DirectoryJournalFactory;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.journal.SuperEngineFactory;

public class StartCommand {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.setProperty("catalina.base", args[0]);

            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("JavaScript");

            Properties ps = EngineFactory.getPropeties();
            String command = args[1];
            String fileName = (String) ps.get(command);

            EngineFactory engineFactory = new EngineFactory();
            engineFactory.createJournaledEngine(new DirectoryJournalFactory(
                    null));

            final Engine engine1 = (Engine) SuperEngineFactory
                    .createTransactionalEngine(
                            engineFactory.journaledEngine,
                            ((JournaledEngine) ((CachedEngine) engineFactory.journaledEngine)
                                    .getSource()).getJournal());

            engine.getContext().setAttribute("engine", engine1,
                    ScriptContext.ENGINE_SCOPE);

            engine.eval(new InputStreamReader(new FileInputStream(fileName),
                    "UTF-8"));
            System.exit(0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}

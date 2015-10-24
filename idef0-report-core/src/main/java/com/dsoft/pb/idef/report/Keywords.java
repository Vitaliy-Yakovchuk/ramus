package com.dsoft.pb.idef.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

public class Keywords extends Hashtable<String, String> {

    /**
     *
     */
    private static final long serialVersionUID = -1638547677877065052L;

    private List<Properties> properties = new ArrayList<Properties>();

    public Keywords() {
        load("_de");
        load("_ru");
        load("_uk");
        load("");
    }

    private void load(String string) {
        Properties ps = new Properties();
        try {
            ps.load(getClass().getResourceAsStream(
                    "/com/ramussoft/idef0/print" + string + ".properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (java.util.Map.Entry<Object, Object> entry : ps.entrySet()) {
            put(entry.getValue().toString(), entry.getKey().toString());
        }
        properties.add(ps);

    }

    public boolean hasName(String value, String connectionName) {
        String s = get(connectionName);
        if (s == null)
            return false;
        return s.equals(value);
    }

    public List<String> getValues(String key) {
        List<String> result = new ArrayList<String>(3);
        for (Properties ps : properties) {
            String value = ps.getProperty(key);
            if (value != null)
                result.add(value);
        }
        return result;
    }
}

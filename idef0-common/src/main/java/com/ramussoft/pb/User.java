package com.ramussoft.pb;

import java.util.Map;

public interface User {

    public int getId();

    public String[] getValues(String attr);

    public String getValue(String attr);

    public void setValue(String attr, String value);

    public void setValues(String attr, String[] values);

    public boolean isAdmin();

    public String[] getAttrs();

    public Map<String, String[]> getAttributes();
}

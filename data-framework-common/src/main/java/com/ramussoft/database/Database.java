package com.ramussoft.database;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;

public interface Database {

    Engine getEngine(String name);

    AccessRules getAccessRules(String name);
}

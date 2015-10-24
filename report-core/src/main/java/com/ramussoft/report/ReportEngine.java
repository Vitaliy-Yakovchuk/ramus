package com.ramussoft.report;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.ramussoft.common.Engine;

public abstract class ReportEngine {

    protected Engine engine;

    public ReportEngine(Engine engine) {
        this.engine = engine;
    }

    public abstract void execute(String scriptPath, OutputStream stream,
                                 Map<String, Object> parameters) throws IOException;
}

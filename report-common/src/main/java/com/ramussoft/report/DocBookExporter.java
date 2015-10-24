package com.ramussoft.report;

import com.ramussoft.gui.common.GUIFramework;

public interface DocBookExporter {

    String getActionName();

    public void createReport(GUIFramework framework, ReportLoadCallback callback);

}

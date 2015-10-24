package com.ramussoft.common.logger;

import java.util.EventListener;

public interface LogListener extends EventListener {

    void logEvent(Event event);

}

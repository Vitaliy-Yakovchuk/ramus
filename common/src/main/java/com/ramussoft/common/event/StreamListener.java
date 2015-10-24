package com.ramussoft.common.event;

import java.util.EventListener;

public interface StreamListener extends EventListener {

    void streamUpdated(StreamEvent event);

    void streamDeleted(StreamEvent event);

}

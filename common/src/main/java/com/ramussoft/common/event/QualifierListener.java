package com.ramussoft.common.event;

import java.util.EventListener;

public interface QualifierListener extends EventListener {

    void qualifierCreated(QualifierEvent event);

    void qualifierDeleted(QualifierEvent event);

    void qualifierUpdated(QualifierEvent event);

    void beforeQualifierUpdated(QualifierEvent event);

}

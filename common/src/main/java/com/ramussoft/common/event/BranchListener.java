package com.ramussoft.common.event;

import java.util.EventListener;

public interface BranchListener extends EventListener {

    void branchCreated(BranchEvent event);

    void branchDeleted(BranchEvent event);

    void branchActivated(BranchEvent event);
}

package com.ramussoft.ai.contract;

import java.util.List;

public record Proposal(String proposalId, String summary, List<ChangeOperation> operations,
                       String contextHash, String provider, ProposalState state) {
    public Proposal {
        operations = List.copyOf(operations);
    }
}


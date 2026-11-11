package com.ramussoft.ai.agent;

import com.ramussoft.ai.contract.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProposalService {
    private final AgentPolicy policy;
    private final Map<String, Proposal> proposals = new ConcurrentHashMap<>();

    public ProposalService(AgentPolicy policy) { this.policy = policy; }

    public Proposal create(String summary, String contextHash, String provider,
                           java.util.List<ChangeOperation> operations) {
        if (!policy.allowRemoval() && operations.stream().anyMatch(RemoveElement.class::isInstance))
            throw new IllegalArgumentException("removal operations are disabled by policy");
        Proposal proposal = new Proposal(UUID.randomUUID().toString(), summary, operations,
                contextHash, provider, ProposalState.PROPOSED);
        proposals.put(proposal.proposalId(), proposal);
        return proposal;
    }

    public Proposal approve(String id, String expectedContextHash) {
        Proposal proposal = require(id);
        if (!proposal.contextHash().equals(expectedContextHash))
            throw new IllegalStateException("context changed; proposal must be regenerated");
        Proposal approved = new Proposal(proposal.proposalId(), proposal.summary(), proposal.operations(),
                proposal.contextHash(), proposal.provider(), ProposalState.APPROVED);
        proposals.put(id, approved);
        return approved;
    }

    public Proposal require(String id) {
        Proposal proposal = proposals.get(id);
        if (proposal == null) throw new IllegalArgumentException("unknown proposal: " + id);
        return proposal;
    }
}

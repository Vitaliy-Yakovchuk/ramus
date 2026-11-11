package com.ramussoft.ai.agent;

import com.ramussoft.ai.contract.*;
import org.junit.Test;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ProposalServiceTest {
    @Test public void requiresMatchingContextOnApproval() {
        ProposalService service = new ProposalService(AgentPolicy.strict());
        Proposal proposal = service.create("rename", "hash-1", "local",
                List.of(new UpdateElement("op-1", "element-1", Map.of("name", "New"), "test")));
        Proposal approved = service.approve(proposal.proposalId(), "hash-1");
        assertEquals(ProposalState.APPROVED, approved.state());
    }

    @Test(expected = IllegalArgumentException.class)
    public void strictPolicyRejectsRemoval() {
        new ProposalService(AgentPolicy.strict()).create("remove", "hash-1", "local",
                List.of(new RemoveElement("op-1", "element-1", "test")));
    }
}

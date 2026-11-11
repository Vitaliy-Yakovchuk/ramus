package com.ramussoft.ai.contract;

public record AgentPolicy(boolean confirmAll, boolean allowCloud, boolean allowRemoval) {
    public static AgentPolicy strict() { return new AgentPolicy(true, false, false); }
}

package com.ramussoft.local.ai;

public record AgentProfile(String name, String type, String endpoint, boolean localOnly, String apiKey, String model) {
    @Override public String toString() { return name + " (" + type + ")"; }
}

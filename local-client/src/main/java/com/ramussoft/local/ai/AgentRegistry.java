package com.ramussoft.local.ai;

import java.util.ArrayList;
import java.util.List;

public final class AgentRegistry {
    private final List<AgentProfile> profiles = new ArrayList<>(List.of(
            new AgentProfile("DeepSeek", "deepseek", "https://api.deepseek.com", false, "", "deepseek-flash")));
    public List<AgentProfile> profiles() { return List.copyOf(profiles); }
    public void add(AgentProfile profile) { profiles.add(profile); }
}

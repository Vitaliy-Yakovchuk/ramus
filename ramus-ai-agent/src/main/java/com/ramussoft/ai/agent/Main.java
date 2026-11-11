package com.ramussoft.ai.agent;

import com.ramussoft.ai.contract.AgentPolicy;
import com.ramussoft.ai.contract.Proposal;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Minimal localhost-only agent. It exposes health and policy, never filesystem or shell access. */
public final class Main {
    private static final AnalysisProvider ANALYZER = new RuleBasedAnalysisProvider();
    private static final ProposalService PROPOSALS = new ProposalService(AgentPolicy.strict());
    public static void main(String[] args) throws Exception {
        int port = args.length == 0 ? 37841 : Integer.parseInt(args[0]);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        AgentPolicy policy = AgentPolicy.strict();
        server.createContext("/v1/health", exchange -> respond(exchange, 200,
                "{\"status\":\"ok\",\"policy\":\"confirm-all\",\"cloud\":false}"));
        server.createContext("/v1/policy", exchange -> respond(exchange, 200,
                "{\"confirmAll\":true,\"allowCloud\":" + policy.allowCloud() +
                        ",\"allowRemoval\":" + policy.allowRemoval() + "}"));
        server.createContext("/v1/analyze", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"error\":\"method not allowed\"}"); return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (body.length() > 2_000_000) { respond(exchange, 413, "{\"error\":\"request too large\"}"); return; }
            String hash = field(body, "contextHash");
            String prompt = field(body, "prompt");
            String snapshot = field(body, "snapshot");
            AnalysisResult analysis = ANALYZER.analyze(prompt, snapshot);
            respond(exchange, 200, "{\"analysisId\":\"local-" + System.currentTimeMillis()
                    + "\",\"contextHash\":\"" + json(hash) + "\",\"provider\":\"local\","
                    + "\"findings\":" + findingsJson(analysis) + ",\"message\":\""
                    + json(analysis.providerMessage()) + "\"}");
        });
        server.createContext("/v1/proposals", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { respond(exchange, 405, "{\"error\":\"method not allowed\"}"); return; }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String hash = field(body, "contextHash");
            Proposal proposal = PROPOSALS.create("Review findings and select typed changes", hash, "local", java.util.List.of());
            respond(exchange, 201, "{\"proposalId\":\"" + json(proposal.proposalId()) + "\",\"state\":\"PROPOSED\",\"contextHash\":\"" + json(hash) + "\",\"operations\":[]}");
        });
        server.setExecutor(null);
        server.start();
        System.out.println("Ramus AI agent listening on http://127.0.0.1:" + port);
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) { output.write(bytes); }
    }

    private static String field(String json, String name) {
        String marker = "\"" + name + "\":\""; int start = json.indexOf(marker);
        if (start < 0) return ""; start += marker.length(); int end = json.indexOf('"', start);
        return end < 0 ? "" : unescape(json.substring(start, end));
    }

    private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }

    private static String unescape(String value) {
        return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String findingsJson(AnalysisResult result) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < result.findings().size(); i++) {
            AnalysisResult.Finding finding = result.findings().get(i);
            if (i > 0) out.append(',');
            out.append("{\"severity\":\"").append(json(finding.severity()))
                    .append("\",\"elementId\":\"").append(json(finding.elementId()))
                    .append("\",\"message\":\"").append(json(finding.message())).append("\"}");
        }
        return out.append(']').toString();
    }
}

package com.ramussoft.local.ai;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Deterministic, read-only projection of the currently open Ramus model. */
public final class ModelSnapshotProvider {
    public ModelSnapshot capture(Engine engine) {
        StringBuilder out = new StringBuilder("ramus-snapshot-v1\n");
        int elements = 0;
        for (Qualifier qualifier : engine.getQualifiers()) {
            if (qualifier.isSystem()) continue;
            out.append("qualifier ").append(qualifier.getId()).append(" ")
                    .append(escape(qualifier.getName())).append("\n");
            for (Element element : engine.getElements(qualifier.getId())) {
                elements++;
                out.append("element ").append(element.getId()).append(" ")
                        .append(qualifier.getId()).append(" ")
                        .append(escape(element.getName())).append("\n");
            }
        }
        return new ModelSnapshot(out.toString(), sha256(out.toString()),
                (int) engine.getQualifiers().stream().filter(q -> !q.isSystem()).count(), elements);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\n", "\\n");
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) { throw new IllegalStateException("SHA-256 unavailable", e); }
    }
}

package com.ramussoft.local.ai;

public record ModelSnapshot(String content, String sha256, int qualifiers, int elements) { }

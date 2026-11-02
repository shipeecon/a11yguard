package com.a11yguard.reporting;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Screenshots {
    private Screenshots() {}

    public static String save(byte[] bytes, Path reportDir, String fileName) {
        if (bytes == null || bytes.length == 0) return "";
        try {
            Path dir = reportDir.resolve("screenshots");
            Files.createDirectories(dir);
            Path out = dir.resolve(safeName(fileName));
            Files.write(out, bytes);
            return "screenshots/" + out.getFileName();
        } catch (Exception e) {
            return "";
        }
    }

    private static String safeName(String name) {
        String cleaned = name == null ? "shot" : name.replaceAll("[^a-zA-Z0-9._-]+", "_");
        if (!cleaned.toLowerCase().endsWith(".png")) cleaned += ".png";
        return cleaned;
    }
}

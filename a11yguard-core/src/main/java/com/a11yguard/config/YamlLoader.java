package com.a11yguard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public final class YamlLoader {
    private static YamlFile cached;

    private YamlLoader() {}

    public static YamlFile load() {
        if (cached != null) return cached;
        Path file = find();
        if (file == null) {
            cached = new YamlFile();
            return cached;
        }
        try {
            ObjectMapper mapper = file.getFileName().toString().endsWith(".json")
                ? new ObjectMapper()
                : new ObjectMapper(new YAMLFactory());
            cached = mapper.readValue(file.toFile(), YamlFile.class);
            if (cached == null) cached = new YamlFile();
            return cached;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read A11yGuard config: " + file, e);
        }
    }

    static Path find() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            for (String name : new String[]{"a11yguard.yaml", "a11yguard.yml", "a11yguard.json"}) {
                Path candidate = dir.resolve(name);
                if (Files.isRegularFile(candidate)) return candidate;
            }
            dir = dir.getParent();
        }
        return null;
    }
}

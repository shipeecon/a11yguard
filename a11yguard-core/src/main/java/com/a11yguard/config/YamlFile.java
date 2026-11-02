package com.a11yguard.config;

import java.util.ArrayList;
import java.util.List;

public class YamlFile {
    public String standard;
    public List<String> failOn;
    public List<String> tags;
    public List<String> exclude;
    public List<String> reportFormats;
    public String reportDir;
    public Boolean screenshots;
    public String baseline;
    public Boolean updateBaseline;
    public Boolean failOnViolations;
    public List<Suppression> suppress = new ArrayList<>();
}

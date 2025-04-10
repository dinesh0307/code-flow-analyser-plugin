package com.dinesh.codeflowanalyser.parser;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;

public class ConfigPropertiesMapper {

    private Map<String, String> configsAndPropertiesMap = new HashMap<>();

    public void parseConfigFiles(List<Path> configFiles) {
        for (Path path : configFiles) {
            try (InputStream input = new FileInputStream(path.toFile())) {
                Properties prop = new Properties();
                prop.load(input);

                for (String key : prop.stringPropertyNames()) {
                    configsAndPropertiesMap.put(key, prop.getProperty(key));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, String> getConfigsAndPropertiesMap() {
        return configsAndPropertiesMap;
    }
}

package com.openway.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration manager for test properties
 */
public class ConfigManager {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "src/test/resources/config.properties";

    static {
        loadProperties();
    }

    /**
     * Load properties from config file
     */
    private static void loadProperties() {
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    properties.load(fis);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
        }
    }

    /**
     * Get property value
     * @param key Property key
     * @return Property value or null if not found
     */
    public static String getProperty(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String environmentProperty = System.getenv(toEnvironmentKey(key));
        if (environmentProperty != null && !environmentProperty.isBlank()) {
            return environmentProperty;
        }

        return properties.getProperty(key);
    }

    private static String toEnvironmentKey(String key) {
        return key.toUpperCase().replace('.', '_').replace('-', '_');
    }
}

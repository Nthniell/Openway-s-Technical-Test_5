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
        return properties.getProperty(key);
    }

    /**
     * Get property value with default
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default value
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get boolean property
     * @param key Property key
     * @return Boolean value
     */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key, "false"));
    }

    /**
     * Get integer property
     * @param key Property key
     * @return Integer value
     */
    public static int getInteger(String key) {
        return Integer.parseInt(properties.getProperty(key, "0"));
    }
}

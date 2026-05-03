package com.openway.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads test configuration from {@code config.properties} and allows environment
 * variables to override individual properties.
 *
 * <p>Resolution order (highest → lowest priority):
 * <ol>
 *   <li>Environment variable (e.g. {@code GMAIL_USERNAME})</li>
 *   <li>JVM system property (e.g. {@code -Dgmail.username=…})</li>
 *   <li>{@code config.properties} entry</li>
 * </ol>
 */
public final class ConfigReader {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                PROPS.load(is);
                LOG.debug("Loaded configuration from {}", CONFIG_FILE);
            } else {
                LOG.warn("Configuration file '{}' not found on the classpath; "
                        + "all values must be supplied via environment variables.", CONFIG_FILE);
            }
        } catch (IOException e) {
            LOG.error("Failed to load configuration file '{}': {}", CONFIG_FILE, e.getMessage());
        }
    }

    private ConfigReader() {
        // utility class
    }

    /**
     * Returns the value for {@code key} by checking, in order:
     * the matching environment variable, the JVM system property, and the properties file.
     *
     * <p>The environment-variable name is derived by upper-casing the key and replacing
     * dots with underscores (e.g. {@code gmail.username} → {@code GMAIL_USERNAME}).
     *
     * @param key property key as it appears in {@code config.properties}
     * @return resolved value, or {@code null} if not set anywhere
     */
    public static String get(String key) {
        // 1. Environment variable
        String envKey = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        // 2. JVM system property
        String sysValue = System.getProperty(key);
        if (sysValue != null && !sysValue.isBlank()) {
            return sysValue;
        }

        // 3. Properties file
        return PROPS.getProperty(key);
    }

    /**
     * Returns the integer value for {@code key}, or {@code defaultValue} if not set
     * or cannot be parsed.
     */
    public static int getInt(String key, int defaultValue) {
        String raw = get(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            LOG.warn("Property '{}' value '{}' is not a valid integer; using default {}.", key, raw, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the boolean value for {@code key}, or {@code defaultValue} if not set.
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String raw = get(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(raw.trim());
    }
}

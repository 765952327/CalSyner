package com.calsync.sync.caldav;

public class CalDavConfig {
    public static final String RADICALE_URL = System.getProperty("radicale.url", System.getenv().getOrDefault("RADICALE_URL", "http://localhost:5232/"));
    public static final String RADICALE_USERNAME = System.getProperty("radicale.username", System.getenv().getOrDefault("RADICALE_USERNAME", ""));
    public static final String RADICALE_PASSWORD = System.getProperty("radicale.password", System.getenv().getOrDefault("RADICALE_PASSWORD", ""));
}

package com.calsync.util;

public class ServiceUtil {
    public static String ensureUrl(String url) {
        if (url == null || url.isEmpty()) return "/";
        if (!url.endsWith("/")) return url + "/";
        return url;
    }
}

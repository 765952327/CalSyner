package com.calsync.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UIDUtils {
    public static String toUid(String str) {
        return UUID.nameUUIDFromBytes(str.getBytes(StandardCharsets.UTF_8)).toString();
    }
    
}

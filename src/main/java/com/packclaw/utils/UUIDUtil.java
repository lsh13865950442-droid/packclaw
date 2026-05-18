package com.packclaw.utils;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * UUID utility class
 */
public final class UUIDUtil {

    private static class Holder {
        static final SecureRandom numberGenerator = getSecureRandom();
    }

    public static String getUUID() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    public static String getRandomSix() {
        return "" + new Random().nextInt(999999);
    }

    public static SecureRandom getSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }
}

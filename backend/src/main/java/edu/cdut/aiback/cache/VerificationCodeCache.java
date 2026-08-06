package edu.cdut.aiback.cache;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class VerificationCodeCache {
    private final Map<String, CodeEntry> cache = new ConcurrentHashMap<>();

    public VerificationCodeCache() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::cleanExpired, 1, 1, TimeUnit.MINUTES);
    }

    public void put(String email, String code, long expireSeconds) {
        long expireTime = System.currentTimeMillis() + expireSeconds * 1000;
        cache.put(email, new CodeEntry(code, expireTime));
    }

    public String get(String email) {
        CodeEntry entry = cache.get(email);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expireTime) {
            cache.remove(email);
            return null;
        }
        return entry.code;
    }

    public void remove(String email) {
        cache.remove(email);
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> now > entry.getValue().expireTime);
    }

    private static class CodeEntry {
        String code;
        long expireTime;
        CodeEntry(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
}

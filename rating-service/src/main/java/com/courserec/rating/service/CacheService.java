package com.courserec.rating.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
  private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public CacheService() {
    // Cleanup expired entries every minute
    scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
  }

  public void putUserValidation(UUID userId, boolean isValid) {
    cache.put("user:" + userId, new CacheEntry(isValid, 60)); // 1 minute TTL
  }

  public Boolean getUserValidation(UUID userId) {
    CacheEntry entry = cache.get("user:" + userId);
    if (entry != null && !entry.isExpired()) {
      return entry.getValue();
    }
    return null;
  }

  public void putCourseValidation(UUID courseId, boolean isValid) {
    cache.put("course:" + courseId, new CacheEntry(isValid, 60)); // 1 minute TTL
  }

  public Boolean getCourseValidation(UUID courseId) {
    CacheEntry entry = cache.get("course:" + courseId);
    if (entry != null && !entry.isExpired()) {
      return entry.getValue();
    }
    return null;
  }

  private void cleanupExpiredEntries() {
    cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
  }

  private static class CacheEntry {
    private final Boolean value;
    private final long expireTime;

    public CacheEntry(Boolean value, long ttlSeconds) {
      this.value = value;
      this.expireTime = System.currentTimeMillis() + (ttlSeconds * 1000);
    }

    public Boolean getValue() {
      return value;
    }

    public boolean isExpired() {
      return System.currentTimeMillis() > expireTime;
    }
  }
}


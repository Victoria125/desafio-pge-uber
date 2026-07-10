package com.vitoria.rideservice.infrastructure.cache;

import com.vitoria.rideservice.domain.RideStatusCache;
import com.vitoria.rideservice.domain.RideStatusData;
import com.vitoria.rideservice.domain.enums.RideStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class RideStatusRedisCache implements RideStatusCache {
    private static final String KEY_PREFIX = "ride:status:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public RideStatusRedisCache(final StringRedisTemplate redisTemplate) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate);
    }

    @Override
    public void put(final String rideId, final RideStatus status, final String driverId) {
        final String key = KEY_PREFIX + rideId;
        this.redisTemplate.opsForHash().putAll(key, Map.of(
                "status", status.name(),
                "driverId", driverId == null ? "" : driverId
        ));
        
        this.redisTemplate.expire(key, TTL);
    }

    @Override
    public Optional<RideStatusData> get(final String rideId) {
        final String key = KEY_PREFIX + rideId;
        final Map<Object, Object> entries = this.redisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        final String driverId = (String) entries.get("driverId");
        return Optional.of(new RideStatusData(
                rideId,
                (String) entries.get("status"),
                driverId == null || driverId.isBlank() ? null : driverId
        ));
    }
}

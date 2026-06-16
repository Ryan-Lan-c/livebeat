package com.livebeat.order.infrastructure.inventory;

import com.livebeat.order.domain.port.InventoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * [order] InventoryPort 的 Redis 實作（Adapter Out）
 *
 * 負責：以 Redis 計數 + Lua 原子扣減保證防超賣（見 docs/10-order-design.md §3-3）。
 *       zone:remaining:{id} 為可售剩餘、zone:ready:{id} 為就緒旗標（缺少時拒絕下單）。
 */
@Component
@RequiredArgsConstructor
class RedisInventoryAdapter implements InventoryPort {

    private static final String REMAINING_KEY = "zone:remaining:";
    private static final String READY_KEY = "zone:ready:";

    /** 回傳：-2 未就緒、-1 售罄、>=0 扣後剩餘。 */
    private static final DefaultRedisScript<Long> RESERVE = new DefaultRedisScript<>(
            """
            if redis.call('EXISTS', KEYS[2]) == 0 then return -2 end
            local remaining = tonumber(redis.call('GET', KEYS[1]) or '-1')
            if remaining < 0 then return -2 end
            if remaining < tonumber(ARGV[1]) then return -1 end
            return redis.call('DECRBY', KEYS[1], ARGV[1])
            """, Long.class);

    /** 僅在 remaining key 存在時回補，避免在未就緒時無中生有。 */
    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
            """
            if redis.call('EXISTS', KEYS[1]) == 0 then return -1 end
            return redis.call('INCRBY', KEYS[1], ARGV[1])
            """, Long.class);

    private final StringRedisTemplate redis;

    @Override
    public Reservation tryReserve(UUID zoneId, int quantity) {
        Long result = redis.execute(RESERVE,
                List.of(REMAINING_KEY + zoneId, READY_KEY + zoneId),
                String.valueOf(quantity));
        long code = (result == null) ? -2L : result;
        if (code == -2L) {
            return Reservation.NOT_READY;
        }
        if (code == -1L) {
            return Reservation.SOLD_OUT;
        }
        return Reservation.RESERVED;
    }

    @Override
    public void release(UUID zoneId, int quantity) {
        redis.execute(RELEASE, List.of(REMAINING_KEY + zoneId), String.valueOf(quantity));
    }

    @Override
    public void warmUp(UUID zoneId, int remaining) {
        redis.opsForValue().set(REMAINING_KEY + zoneId, String.valueOf(remaining));
        redis.opsForValue().set(READY_KEY + zoneId, "1");
    }

    @Override
    public Long remaining(UUID zoneId) {
        String value = redis.opsForValue().get(REMAINING_KEY + zoneId);
        return (value == null) ? null : Long.parseLong(value);
    }
}

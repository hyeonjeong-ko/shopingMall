package goorm.server.timedeal.config.redis;

import static goorm.server.timedeal.config.exception.BaseResponseStatus.LOCK_ACQUISITION_FAILED;
import static goorm.server.timedeal.config.exception.BaseResponseStatus.STOCK_DECREASE_FAILED;

import goorm.server.timedeal.config.exception.domain.TimeDealException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimeDealCache {
    private static final String STOCK_KEY_PREFIX = "time_deal:stock:";
    private static final String LOCK_KEY_PREFIX = "deal-lock:";
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    public TimeDealCache(RedissonClient redissonClient, StringRedisTemplate redisTemplate) {
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
    }

    public Integer getStock(Long timeDealId) {
        String stockKey = STOCK_KEY_PREFIX + timeDealId;
        String stockValue = redisTemplate.opsForValue().get(stockKey);
        return stockValue != null ? Integer.parseInt(stockValue) : null;
    }

    public void setStock(Long timeDealId, int quantity) {
        String stockKey = STOCK_KEY_PREFIX + timeDealId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(quantity));
    }

    // TimeDealCache.java
    public boolean decreaseStockwithLock(Long timeDealId, int quantity) {
        String stockKey = STOCK_KEY_PREFIX + timeDealId;
        RLock lock = getLock(timeDealId);

        try {
            // 락 획득 시도 (최대 5초 대기, 락 유지 시간 3초)
            boolean isLocked = lock.tryLock(5, 3, TimeUnit.SECONDS);
            if (!isLocked) {
                log.error("재고 감소 락 획득 실패 - TimeDeal ID: {}", timeDealId);
                throw new TimeDealException(LOCK_ACQUISITION_FAILED);
            }

            // 현재 재고 확인
            Integer currentStock = getStock(timeDealId);
            if (currentStock == null) {
                log.error("재고 정보 없음 - TimeDeal ID: {}", timeDealId);
                return false;
            }

            // 재고 부족 체크
            if (currentStock < quantity) {
                log.warn("재고 부족 - TimeDeal ID: {}, 요청수량: {}, 현재재고: {}",
                        timeDealId, quantity, currentStock);
                return false;
            }

            // 재고 감소
            setStock(timeDealId, currentStock - quantity);
            log.info("재고 감소 성공 - TimeDeal ID: {}, 감소수량: {}, 남은재고: {}",
                    timeDealId, quantity, currentStock - quantity);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("재고 감소 중 인터럽트 발생 - TimeDeal ID: {}", timeDealId, e);
            throw new TimeDealException(STOCK_DECREASE_FAILED);
        } catch (Exception e) {
            log.error("재고 감소 중 예외 발생 - TimeDeal ID: {}", timeDealId, e);
            throw new TimeDealException(STOCK_DECREASE_FAILED);
        } finally {
            // 락이 현재 스레드에 의해 보유중인 경우에만 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("재고 감소 락 해제 - TimeDeal ID: {}", timeDealId);
            }
        }
    }

    public RLock getLock(Long timeDealId) {
        return redissonClient.getLock(LOCK_KEY_PREFIX + timeDealId);
    }

    public void evictStock(Long timeDealId) {
        String stockKey = STOCK_KEY_PREFIX + timeDealId;
        redisTemplate.delete(stockKey);
    }
}
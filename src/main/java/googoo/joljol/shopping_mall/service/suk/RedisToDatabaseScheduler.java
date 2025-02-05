package googoo.joljol.shopping_mall.service.suk;

import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.repository.suk.ShoppingMallStatsBatchRepository;
import googoo.joljol.shopping_mall.service.suk.v2.CacheableShoppingMallServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisToDatabaseScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheableShoppingMallServiceV2 cacheableShoppingMallServiceV2;
    private final ShoppingMallStatsBatchRepository batchRepository;

    @Scheduled(fixedRate = 1000 * 60 * 1) // 10분마다 실행 1000 * 60 * 10
    @Transactional
    public void syncRedisToDatabase() {
        log.info("🔄 10분마다 Redis 데이터를 DB로 동기화 시작...");
        Long zSetSize = redisTemplate.opsForZSet().size("top100Malls:zset");
        long hashSize = redisTemplate.opsForHash().size("top100Malls:hash");

        // 임시 방편
        if (zSetSize != hashSize) return;

        List<ShoppingMallResponseDto> cacheData = cacheableShoppingMallServiceV2.getCacheData(zSetSize);
        if (cacheData.isEmpty()) return;

        batchRepository.updateViewCountInBatch(cacheData);
    }
}

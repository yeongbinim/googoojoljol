package googoo.joljol.shopping_mall.service.suk.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CacheableShoppingMallServiceV2 {

    private final ShoppingMallStatsRepository shoppingMallStatsRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private final int RANKING_NUM = 100;
    public final String ZSET_CACHE_KEY = "top100Malls:zset";
    public final String HASH_CACHE_KEY = "top100Malls:hash";

    private ZSetOperations<String, Object> zSetOps;
    private HashOperations<String, String, Object> hashOps;

    @PostConstruct
    private void initRedisOperations() {
        this.zSetOps = redisTemplate.opsForZSet();
        this.hashOps = redisTemplate.opsForHash();
    }

    // V2: Zset, Hash 자료구조를 이용해 쇼핑몰 랭킹 Caching
    public List<ShoppingMallResponseDto> getTopShoppingMallsBySortedSet(int top) {

        // 데이터 캐싱 안 되어 있으면 Caching
        if (zSetOps.size(ZSET_CACHE_KEY) < RANKING_NUM || hashOps.size(HASH_CACHE_KEY) < RANKING_NUM) {
            redisTemplate.delete(ZSET_CACHE_KEY);
            redisTemplate.delete(HASH_CACHE_KEY);

            insertCacheData();
            redisTemplate.expire(ZSET_CACHE_KEY, Duration.ofHours(12));
            redisTemplate.expire(HASH_CACHE_KEY, Duration.ofHours(12));
        }

        List<ShoppingMallResponseDto> result = getCacheData(top);
        return result;
    }

    private void insertCacheData() {
        shoppingMallStatsRepository.findHotShoppingMallByOrderByViewCountDesc(RANKING_NUM).forEach(
                response -> {
                    storeZsetInRedis(ZSET_CACHE_KEY, response.getId(), response.getViewCount()); // ZSet 저장
                    storeHashInRedis(HASH_CACHE_KEY, response);
                }
        );
    }

    private void storeZsetInRedis(String cacheKey, Long id, Integer score) {
        zSetOps.add(cacheKey, id.toString(), score);
    }

    private void storeHashInRedis(String cacheKey, ShoppingMallResponseDto response) {
        hashOps.put(cacheKey, response.getId().toString(), response);
    }

    public void updateViewCountRanking(ShoppingMall shoppingMall, ShoppingMallStats stats) {
        // 랭킹에 있는지 확인
        Double score = zSetOps.score(ZSET_CACHE_KEY, shoppingMall.getId().toString());
        Object obj = hashOps.get(HASH_CACHE_KEY, shoppingMall.getId().toString());

        if (score != null && obj != null) {
            updateZSetRanking(shoppingMall.getId());
            updateHashRanking(shoppingMall.getId());
        } else { // 하나라도 랭킹에 없으면 추가
            storeZsetInRedis(ZSET_CACHE_KEY, shoppingMall.getId(), stats.getViewCount());
            storeHashInRedis(HASH_CACHE_KEY, ShoppingMallResponseDto.from(shoppingMall, stats));
        }
    }

    private void updateZSetRanking(Long shoppingMallId) {
        zSetOps.incrementScore(ZSET_CACHE_KEY, shoppingMallId.toString(), 1); // atomic 연산
    }

    // Hash는 value 자체에 대한 Atomic 연산을 지원하지만, value에 객체가 저장되어 있어. 귀찮아짐.
    private void updateHashRanking(Long shoppingMallId) {

        Object obj = hashOps.get(HASH_CACHE_KEY, shoppingMallId.toString());

        ShoppingMallResponseDto response = objectMapper.convertValue(obj, ShoppingMallResponseDto.class);
        response.increaseViewCount();

        if (obj != null) {
            hashOps.put(HASH_CACHE_KEY, response.getId().toString(), response);
        }
    }

    // 예외 처리 해줘야 하는데... 흠
    public List<ShoppingMallResponseDto> getCacheData(long size) {
        Map<String, Object> entries = hashOps.entries(HASH_CACHE_KEY);
        Set<ZSetOperations.TypedTuple<Object>> cache = zSetOps.reverseRangeWithScores(ZSET_CACHE_KEY, 0, size - 1);

        if (cache == null) {
            return List.of();
        }

        return cache.stream()
                .map(tuple -> {
                    String id = tuple.getValue().toString();
                    int score = (int) Double.parseDouble(tuple.getScore().toString()); // hash에 있는 viewCount는 안 바뀐다...

                    ShoppingMallResponseDto response = objectMapper.convertValue(entries.get(id), ShoppingMallResponseDto.class);
                    if (score > response.getViewCount()) { // 불일치 할 경우. ZSet은 atomic하게 들어가는데, Hash는 불일치할 수 있어서 score를 우선시
                        response.setViewCount(score);
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }
}

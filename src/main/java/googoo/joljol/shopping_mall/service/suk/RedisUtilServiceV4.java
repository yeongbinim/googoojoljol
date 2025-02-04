package googoo.joljol.shopping_mall.service.suk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import googoo.joljol.shopping_mall.entity.ShoppingMallForRedis;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisUtilServiceV4 {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ShoppingMallRepository shoppingMallRepository;


//    public HashOperations<String, String, String> getHashOps() {
//        return redisTemplate.opsForHash();
//    }

    public void ensureCacheIsUpdated(String cacheKey, long cacheTTL, int batchSize) {
        Long cacheSize = redisTemplate.opsForHash().size(cacheKey);
        Long dbCount = shoppingMallRepository.count();

        if (cacheSize == null || cacheSize.equals(dbCount) == false) {
            insertDataIntoRedisHashASync(cacheKey, cacheTTL, batchSize); // Asynchronous
        }
    }

    @Async
    public CompletableFuture<Void> insertDataIntoRedisHashASync(String cacheKey, long cacheTTL, int batchSize) {
        log.info("✅ Redis List 동기 데이터 저장 시작");
        redisTemplate.delete(cacheKey);

//        List<ShoppingMallForRedis> allShoppingMalls = shoppingMallRepository.findAll()
        List<ShoppingMallForRedis> allShoppingMalls = shoppingMallRepository.findAllLimit(2000)
                .stream()
                .map(ShoppingMallForRedis::new)
                .toList(); // 여기서 메모리가 좀 많이 필요할 듯
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        // `CompletableFuture` 리스트로 모든 병렬 작업을 추적 (데이터 13만 개를 배치 크기(BATCH_SIZE)만큼 나누어 비동기 처리)
        List<CompletableFuture<Void>> futures = IntStream.range(0, (allShoppingMalls.size() + batchSize - 1) / batchSize)
                .mapToObj(batchIndex -> CompletableFuture.runAsync(() -> {
                    int start = batchIndex * batchSize;
                    int end = Math.min(start + batchSize, allShoppingMalls.size());

                    for (int i = start; i < end; i++) {
                        ShoppingMallForRedis shoppingMall = allShoppingMalls.get(i);
                        storeHashInRedis(hashOps, cacheKey, shoppingMall);
                    }
                }))
                .collect(Collectors.toList());

        // 모든 작업이 끝날 때까지 기다린 후 TTL 설정
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenRun(() -> {
                    redisTemplate.expire(cacheKey, cacheTTL, TimeUnit.SECONDS);
                    log.info("✅ Redis List 비동기 데이터 저장 완료");
                });
    }

    // Redis에 List 데이터 저장
    private void storeHashInRedis(HashOperations<String, String, String> hashOps, String cacheKey, ShoppingMallForRedis mall) {
        try {
            String serializedShoppingMall = objectMapper.writeValueAsString(mall);
            hashOps.put(cacheKey, mall.getId().toString(), serializedShoppingMall);
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 직렬화 실패 - ID: {}, 오류: {}", mall.getId(), e.getMessage());
        }

//        hashOps.put(cacheKey, mall.getId().toString(), mall);
    }
}

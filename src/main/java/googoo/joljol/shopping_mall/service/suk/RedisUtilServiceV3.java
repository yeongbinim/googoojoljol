package googoo.joljol.shopping_mall.service.suk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
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
public class RedisUtilServiceV3 {

    private final RedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ShoppingMallRepository shoppingMallRepository;


    public ZSetOperations getZSetOps() {
        return redisTemplate.opsForZSet();
    }

    public void ensureCacheIsUpdated(String cacheKey, long cacheTTL, int batchSize) {
        log.info("currentThread-{}", Thread.currentThread().getName());
        Long cacheSize = redisTemplate.opsForZSet().size(cacheKey);
        Long dbCount = shoppingMallRepository.count();

        if (cacheSize == null || cacheSize.equals(dbCount) == false) {
//            insertDataIntoRedisWithZSetSync(cacheKey, cacheTTL); // 👺Synchronous
            CompletableFuture<Void> future = insertDataIntoRedisWithZSetAsync(cacheKey, cacheTTL, batchSize);// Asynchronous
//            future.join(); // 👀️ 단일 `CompletableFuture`의 완료 대기 (현재 스레드를 블로킹(blocking)하여 결과를 반환) 이 친구를 실행해줘야 정상적으로 API 실행
        }
    }

    @Async
    public CompletableFuture<Void> insertDataIntoRedisWithZSetAsync(String cacheKey, long cacheTTL, int batchSize) {
        log.info("✅ Redis ZSet 비동기 데이터 저장 시작");
        redisTemplate.delete(cacheKey);

        List<ShoppingMall> allShoppingMalls = shoppingMallRepository.findAll();
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // `CompletableFuture` 리스트로 모든 병렬 작업을 추적 (데이터 13만 개를 배치 크기(BATCH_SIZE)만큼 나누어 비동기 처리)
        List<CompletableFuture<Void>> futures = IntStream.range(0, (allShoppingMalls.size() + batchSize - 1) / batchSize)
                .mapToObj(batchIndex -> CompletableFuture.runAsync(() -> {
                    log.info("🚀currentThread-{}", Thread.currentThread().getName());
                    int start = batchIndex * batchSize;
                    int end = Math.min(start + batchSize, allShoppingMalls.size());

                    for (int i = start; i < end; i++) {
                        ShoppingMall shoppingMall = allShoppingMalls.get(i);
                        storeZSetInRedis(zSetOps, cacheKey, shoppingMall);
                    }
                }))
                .collect(Collectors.toList());

        /*
          * 🚀 모든 작업이 끝날 때까지 기다린 후 TTL 설정
          * CompletableFuture.allOf(): 여러 개의 비동기 작업이 완료될 떄까지 대기 (`allOf()` 자체는 개별 결과를 반환하지 않고, 단순히 작업이 끝났다는 신호만 제공)
          * Completable.runAsync(): 얘는 그냥 콜백 안 받고 바로 실행
         */
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenRun(() -> {
                    redisTemplate.expire(cacheKey, cacheTTL, TimeUnit.SECONDS);
                    log.info("✅ Redis ZSet 비동기 데이터 저장 완료");
                });
    }

    // 레디스에 데이터 동기 삽입
    public void insertDataIntoRedisWithZSetSync(String cacheKey, long cacheTTL) {
        log.info("✅ Redis ZSet 동기 데이터 저장 시작");
        redisTemplate.delete(cacheKey);

        List<ShoppingMall> allShoppingMalls = shoppingMallRepository.findAll();
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        allShoppingMalls.forEach(shoppingMall -> storeZSetInRedis(zSetOps, cacheKey, shoppingMall));

        // Redis TTL 설정 (1시간)
        redisTemplate.expire(cacheKey, cacheTTL, TimeUnit.SECONDS);
        log.info("✅ Redis ZSet 동기 데이터 저장 요청 완료");
    }

    // 레디스에 저장
    public void storeZSetInRedis(ZSetOperations<String, String> zSetOps, String cacheKey, ShoppingMall mall) {
        try {
            String json = objectMapper.writeValueAsString(mall);
            double score = -mall.getMonitoringDate().toEpochDay(); // 날짜 기반 정렬
            zSetOps.add(cacheKey, json, score);
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 직렬화 실패 - ID: {}, 오류: {}", mall.getId(), e.getMessage());
        }
    }

}

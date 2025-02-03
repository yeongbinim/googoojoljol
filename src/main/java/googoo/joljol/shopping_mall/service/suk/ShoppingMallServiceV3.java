package googoo.joljol.shopping_mall.service.suk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
  * 레디스에 ZSet 타입으로 전체 데이터 저장
  * `overallRating` & `businessStatus` 로 필터링 -> 너무 느리다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingMallServiceV3 {

    private final ShoppingMallRepository shoppingMallRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final String CACHE_KEY = "shopping_mall:zset";
    private static final long CACHE_TTL = 7200;
    private static final int BATCH_SIZE = 1000;


    public Page<ShoppingMall> getFilteredShoppingMallsV3UsingCache(Integer overallRating, String businessStatus, Pageable pageable) {
        ensureCacheIsUpdated();

        Set<String> resultSet = redisTemplate.opsForZSet().range(CACHE_KEY, 0, -1);
        if (resultSet == null || resultSet.isEmpty()) {
            return Page.empty(pageable);
        }

        List<ShoppingMall> filteredList = resultSet.stream()
                .map(this::deserializeJson)
                .filter(shoppingMall -> shoppingMall != null && matchesFilter(shoppingMall, overallRating, businessStatus))
                .collect(Collectors.toList());

        return applyPagination(filteredList, pageable);
    }

    // 조건 검색
    private boolean matchesFilter(ShoppingMall mall, Integer overallRating, String businessStatus) {
        return (overallRating == null || Objects.equals(mall.getOverallRating(), overallRating))
                && (businessStatus == null || mall.getBusinessStatus().contains(businessStatus));
    }

    // 페이징 처리
    private Page<ShoppingMall> applyPagination(List<ShoppingMall> list, Pageable pageable) {
        int start = Math.min(pageable.getPageNumber() * pageable.getPageSize(), list.size());
        int end = Math.min(start + pageable.getPageSize(), list.size());

        List<ShoppingMall> pagedList = list.subList(start, end);
        return new PageImpl<>(pagedList, pageable, list.size());
    }

    // 캐시 체크
    private void ensureCacheIsUpdated() {
        Long cacheSize = redisTemplate.opsForZSet().size(CACHE_KEY);
        Long dbCount = shoppingMallRepository.count();

        if (cacheSize == null || cacheSize.equals(dbCount) == false) {
//            insertDataIntoRedisWithZSetSync(); // Synchronous
            insertDataIntoRedisWithZSetAsync(); // Asynchronous
        }
    }

    // 레디스에 데이터 동기 삽입
    private void insertDataIntoRedisWithZSetSync() {
        log.info("✅ Redis ZSet 동기 데이터 저장 시작");
        redisTemplate.delete(CACHE_KEY);

        List<ShoppingMall> allShoppingMalls = shoppingMallRepository.findAll();
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        allShoppingMalls.forEach(shoppingMall -> storeInRedis(zSetOps, shoppingMall));

        // Redis TTL 설정 (1시간)
        redisTemplate.expire(CACHE_KEY, CACHE_TTL, TimeUnit.SECONDS);
        log.info("✅ Redis ZSet 동기 데이터 저장 요청 완료");
    }

    // 레디스에 데이터 비동기 삽입
    @Async
    public CompletableFuture<Void> insertDataIntoRedisWithZSetAsync() {
        log.info("✅ Redis ZSet 비동기 데이터 저장 시작");
        redisTemplate.delete(CACHE_KEY);

        List<ShoppingMall> allShoppingMalls = shoppingMallRepository.findAll();
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // `CompletableFuture` 리스트로 모든 병렬 작업을 추적 (데이터 13만 개를 배치 크기(BATCH_SIZE)만큼 나누어 비동기 처리)
        List<CompletableFuture<Void>> futures = IntStream.range(0, (allShoppingMalls.size() + BATCH_SIZE - 1) / BATCH_SIZE)
                .mapToObj(batchIndex -> CompletableFuture.runAsync(() -> {
                    int start = batchIndex * BATCH_SIZE;
                    int end = Math.min(start + BATCH_SIZE, allShoppingMalls.size());

                    for (int i = start; i < end; i++) {
                        ShoppingMall shoppingMall = allShoppingMalls.get(i);
                        storeInRedis(zSetOps, shoppingMall);
                    }
                }))
                .collect(Collectors.toList());

        // 모든 작업이 끝날 때까지 기다린 후 TTL 설정
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenRun(() -> {
                    redisTemplate.expire(CACHE_KEY, CACHE_TTL, TimeUnit.SECONDS);
                    log.info("✅ Redis ZSet 비동기 데이터 저장 완료");
                });
    }

    // 레디스에 저장
    private void storeInRedis(ZSetOperations<String, String> zSetOps, ShoppingMall mall) {
        try {
            String json = objectMapper.writeValueAsString(mall);
            double score = -mall.getMonitoringDate().toEpochDay(); // 날짜 기반 정렬
            zSetOps.add(CACHE_KEY, json, score);
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 직렬화 실패 - ID: {}, 오류: {}", mall.getId(), e.getMessage());
        }
    }

    // 역직렬화 공통 메서드
    private ShoppingMall deserializeJson(String json) {
        try {
            return objectMapper.readValue(json, ShoppingMall.class);
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 역직렬화 실패: {}", e.getMessage());
            return null;
        }
    }
}

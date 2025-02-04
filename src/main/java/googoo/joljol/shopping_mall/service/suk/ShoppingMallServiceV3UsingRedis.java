package googoo.joljol.shopping_mall.service.suk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * 레디스에 ZSet 타입으로 전체 데이터 저장
 * `overallRating` & `businessStatus` 로 필터링 -> 너무 느리다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingMallServiceV3UsingRedis {

    private final RedisUtilServiceV3 redisUtilServiceV3;
    private final ObjectMapper objectMapper;

    private final String CACHE_KEY = "shopping_mall:zset";
    private final long CACHE_TTL = 7200;
    private final int BATCH_SIZE = 1000;

    public Page<ShoppingMall> getFilteredShoppingMallsV3UsingCache(Integer overallRating, String businessStatus, Pageable pageable) {
        redisUtilServiceV3.ensureCacheIsUpdated(CACHE_KEY, CACHE_TTL, BATCH_SIZE);

        log.info("🍎currentThread-{}", Thread.currentThread().getName());

        Set<String> resultSet = redisUtilServiceV3.getZSetOps().range(CACHE_KEY, 0, -1);
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

    private ShoppingMall deserializeJson(String json) {
        try {
            return objectMapper.readValue(json, ShoppingMall.class);
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 역직렬화 실패: {}", e.getMessage());
            return null;
        }
    }
}

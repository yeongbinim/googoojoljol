package googoo.joljol.shopping_mall.service.suk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redislabs.lettusearch.*;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallForRedis;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.Limit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static googoo.joljol.shopping_mall.service.suk.index.ShoppingMallIndexForRediSearch.INDEX_NAME;

/*
 * `RediSearch`를 이용한 조회 테스트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingMallServiceV4UsingRedisSearch {

    private final RedisUtilServiceV4 redisUtilServiceV4;
    private final StatefulRediSearchConnection<String, String> searchConnection;
    private final RedisClient redisClient;
    private RedisCommands<String, String> redisCommands;

    private final String CACHE_KEY = "shopping_mall:";
    private static final long CACHE_TTL = 7200;
    private static final int BATCH_SIZE = 1000;


    public Page<ShoppingMallForRedis> getFilteredShoppingMallsV3UsingRediSearch(
            Integer overallRating, String businessStatus, Pageable pageable) {

        // 데이터 동기화
        redisUtilServiceV4.ensureCacheIsUpdated(CACHE_KEY, CACHE_TTL, BATCH_SIZE);

        return null;

//        RediSearchCommands<String, String> commands = searchConnection.sync();
////        String query = buildQuery(overallRating, businessStatus);
//        String query = "*";
//
//        SearchResults<String, String> searchResults = commands.search(
//                INDEX_NAME,
//                query,
//                SearchOptions.<String>builder()
//                        .limit(SearchOptions.Limit.builder()
//                                .offset(0)
//                                .num(10)
//                                .build())
//
//                        .build()
//        );
//
//        // 검색 결과를 객체 리스트로 변환
//        List<ShoppingMallForRedis> results = searchResults.stream()
//                .map(ShoppingMallForRedis::convertToShoppingMallForRedis)
//                .toList();
//
//        // 페이징하여 반환
//        return new PageImpl<>(results, pageable, searchResults.getCount());
    }

    // 검색 조건을 위한 RediSearch 쿼리 빌드
    private String buildQuery(Integer overallRating, String businessStatus) {
        StringBuilder queryBuilder = new StringBuilder("*"); // 기본 검색

        if (overallRating != null) {
            // ✅ 범위 검색 수정: 올바른 숫자 범위 지정
            queryBuilder.append(" @overallRating:[").append(overallRating).append(" ").append(Integer.MAX_VALUE).append("]");
        }

        if (businessStatus != null) {
            // ✅ TEXT 검색 방식 적용
            queryBuilder.append(" @businessStatus:\"").append(businessStatus).append("\"");
        }

        return queryBuilder.toString();
    }


}

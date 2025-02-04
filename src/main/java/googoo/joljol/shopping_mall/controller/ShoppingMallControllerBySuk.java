package googoo.joljol.shopping_mall.controller;

import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallForRedis;
import googoo.joljol.shopping_mall.service.suk.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShoppingMallControllerBySuk {

    private final ProxyShoppingMallService proxyShoppingMallService;
    private final ShoppingMallServiceV2 shoppingMallServiceV2;
    private final ShoppingMallServiceV3UsingRedis shoppingMallServiceV3UsingRedis;
    private final ShoppingMallServiceV4UsingRedisSearch shoppingMallServiceV4UsingRedisSearch;


    // index 성능 개선
    @GetMapping("/shopping-mall/v2")
    public ResponseEntity<Page<ShoppingMall>> getShoppingMallsV2UsingIndex(
            @RequestParam(required = false) Integer overallRating,
            @RequestParam(required = false) String businessStatus,
            @PageableDefault Pageable pageable
    ) {
        Page<ShoppingMall> response = shoppingMallServiceV2.getFilteredShoppingMallsV2UsingIndex(
                overallRating, businessStatus, pageable
        );
        return ResponseEntity.ok(response);
    }

    // using redis
    @GetMapping("/shopping-mall/v3")
    public ResponseEntity<Page<ShoppingMall>> getShoppingMallsV3ByCache(
            @RequestParam(required = false) Integer overallRating,
            @RequestParam(required = false) String businessStatus,
            @PageableDefault Pageable pageable
    ) {
        Page<ShoppingMall> response = shoppingMallServiceV3UsingRedis.getFilteredShoppingMallsV3UsingCache(
                overallRating, businessStatus, pageable
        );
        return ResponseEntity.ok(response);
    }

    // using redisearch
    @GetMapping("/shopping-mall/v4")
    public ResponseEntity<Page<ShoppingMallForRedis>> getShoppingMallsV4UsingRediSearch(
            @RequestParam(required = false) Integer overallRating,
            @RequestParam(required = false) String businessStatus,
            @PageableDefault Pageable pageable
    ) {
        Page<ShoppingMallForRedis> response = shoppingMallServiceV4UsingRedisSearch.getFilteredShoppingMallsV3UsingRediSearch(
                overallRating, businessStatus, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shopping-mall/hot")
    public ResponseEntity<List<ShoppingMallResponseDto>> getTop10ShoppingMalls(@RequestParam int top) {
        return ResponseEntity.ok(proxyShoppingMallService.getHotShoppingMalls(top));
    }

    @GetMapping("/shopping-mall/view/{id}")
    public ResponseEntity<ShoppingMall> getShoppingMall(@PathVariable Long id, @RequestParam Integer viewCount) {
        ShoppingMall response = proxyShoppingMallService.getShoppingMallById(id, viewCount);
        return ResponseEntity.ok(response);
    }

}

package googoo.joljol.shopping_mall.service.suk.v1;

import googoo.joljol.common.exception.CustomException;
import googoo.joljol.common.exception.ExceptionType;
import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import googoo.joljol.shopping_mall.service.suk.ViewHistoryManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static googoo.joljol.common.exception.ExceptionType.SHOPPING_MALL_NOT_FOUND;
import static googoo.joljol.shopping_mall.service.suk.ViewHistoryManager.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProxyShoppingMallServiceV1 {

    private final CacheableShoppingMallServiceV1 cacheableShoppingMallServiceV1;
    private final ShoppingMallStatsRepository shoppingMallStatsRepository;
    private final ShoppingMallRepository shoppingMallRepository;
    private final ViewHistoryManager viewHistoryManager;

    // @Cacheable 사용
    public List<ShoppingMallResponseDto> getTopRankingShoppingMallsV1(int top) {
        if (top < 10 || top > 100) {
            throw new CustomException(ExceptionType.SHOPPING_MALL_HOT_RANK_BAD_REQUEST);
        }
        List<ShoppingMallResponseDto> cachedData = cacheableShoppingMallServiceV1.getTopShoppingMallsByCacheable();
        return cachedData.subList(0, Math.min(top, cachedData.size()));
    }

    @Transactional // 좋지 않다. 조회만 하는 게 아니라, ShoppingMallStats이 없으면 생성까지 해야 해서 write모드. 이거
    public ShoppingMall getShoppingMallByIdV1(Long id, String ip) {
        ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
                .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        ViewHistory viewHistory = viewHistoryManager.findViewHistory(shoppingMall.getId(), ip);
        if (viewHistory == null) { // 5분 이내 조회 이력이 있는지
            log.info("5분 이내 {} -> {} 조회이력 없음", ip, id);
            updateViewCountV1(shoppingMall, ip);
        } else {
            log.warn("5분 이내 {} -> {} 조회이력 있음", ip, id);
        }
        return shoppingMall;
    }

    // V1은 조회하면 바로 DB에 반영. 그렇기에 동시성 이슈가 발생.
    public void updateViewCountV1(ShoppingMall shoppingMall, String ip) {
        ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(shoppingMall.getId())
                .orElse(new ShoppingMallStats(null, shoppingMall, 0));

        if (stats.getId() == null) { // 없으면 생성. 이것때문에 Transaction write 모드인데 직관적이지는 않다.
            shoppingMallStatsRepository.save(stats);
        }

        stats.incrementViewCount();
        viewHistoryManager.saveViewHistory(shoppingMall.getId(), ip);
    }


}

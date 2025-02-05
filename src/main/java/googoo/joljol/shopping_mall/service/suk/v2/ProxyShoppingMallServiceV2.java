package googoo.joljol.shopping_mall.service.suk.v2;

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
public class ProxyShoppingMallServiceV2 {

    private final CacheableShoppingMallServiceV2 cacheableShoppingMallServiceV2;
    private final ShoppingMallRepository shoppingMallRepository;
    private final ShoppingMallStatsRepository shoppingMallStatsRepository;
    private final ViewHistoryManager viewHistoryManager;

    // SortedSet 사용
    public List<ShoppingMallResponseDto> getTopRankingShoppingMallsV2(int top) {
        if (top < 10 || top > 100) {
            throw new CustomException(ExceptionType.SHOPPING_MALL_HOT_RANK_BAD_REQUEST);
        }
        return cacheableShoppingMallServiceV2.getTopShoppingMallsBySortedSet(top);
    }

    @Transactional
    // 하나의 상점 조회
    public ShoppingMall getShoppingMallByIdV2(Long id, String ip) {
        ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
                .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        ViewHistory viewHistory = viewHistoryManager.findViewHistory(shoppingMall.getId(), ip);
        if (viewHistory == null) { // 5분 이내 조회 이력이 있는지
            log.info("5분 이내 {} -> {} 조회이력 없음", ip, id);
            updateViewCountV2(shoppingMall, ip);

        } else {
            log.warn("5분 이내 {} -> {} 조회이력 있음", ip, id);
        }
        return shoppingMall;
    }

    public void updateViewCountV2(ShoppingMall shoppingMall, String ip) {
        ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(shoppingMall.getId())
                .orElse(new ShoppingMallStats(null, shoppingMall, 1));

        if (stats.getId() == null) { // 업으면 생성. 하지만 V2에서는 업데이트를 배치로 한 번에 할 예정
            shoppingMallStatsRepository.save(stats);
        }

        viewHistoryManager.saveViewHistory(shoppingMall.getId(), ip);
        cacheableShoppingMallServiceV2.updateViewCountRanking(shoppingMall, stats);
    }

}

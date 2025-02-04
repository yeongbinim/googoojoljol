package googoo.joljol.shopping_mall.service.suk;

import googoo.joljol.common.exception.CustomException;
import googoo.joljol.common.exception.ExceptionType;
import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static googoo.joljol.common.exception.ExceptionType.SHOPPING_MALL_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProxyShoppingMallService {

    private final CacheableShoppingMallService cacheableShoppingMallService;
    private final ShoppingMallRepository shoppingMallRepository;
    private final ShoppingMallStatsRepository shoppingMallStatsRepository;

    public List<ShoppingMallResponseDto> getHotShoppingMalls(int top) {
        if (top < 10 || top > 100) {
            throw new CustomException(ExceptionType.SHOPPING_MALL_HOT_RANK_BAD_REQUEST);
        }
        List<ShoppingMallResponseDto> cachedData = cacheableShoppingMallService.getTop100ShoppingMalls();
        return cachedData.subList(0, Math.min(top, cachedData.size()));
    }

    @Transactional
    public ShoppingMall getShoppingMallById(Long id, Integer viewCount) {
        ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
                .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        updateViewCount(shoppingMall, viewCount);

        return shoppingMall;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected void updateViewCount(ShoppingMall shoppingMall, int viewCount) {
        ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(shoppingMall.getId())
                .orElse(new ShoppingMallStats(null, shoppingMall, 0));

        if (stats.getId() == null) {
            shoppingMallStatsRepository.save(stats);
        }

        stats.increaseViewCountBy(viewCount);
    }
}

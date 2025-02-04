package googoo.joljol.shopping_mall.service.lock;

import static googoo.joljol.common.exception.ExceptionType.SHOPPING_MALL_NOT_FOUND;
import static googoo.joljol.common.exception.ExceptionType.SHOPPING_MALL_STATS_NOT_FOUND;

import googoo.joljol.common.exception.CustomException;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingMallOptimisticLockService {

    private final ShoppingMallRepository shoppingMallRepository;
    private final ShoppingMallStatsRepository shoppingMallStatsRepository;

    @Transactional
    public ShoppingMall callOptimisticLock(Long id) {
        ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        ShoppingMallStats stats = shoppingMallStatsRepository
            .findByShoppingMallIdWithOptimisticLock(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_STATS_NOT_FOUND));

        stats.incrementViewCount();
        shoppingMallStatsRepository.save(stats);

        return shoppingMall;
    }

}

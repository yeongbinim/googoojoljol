package googoo.joljol.shopping_mall.service.lock;

import static googoo.joljol.common.exception.ExceptionType.SHOPPING_MALL_NOT_FOUND;

import googoo.joljol.common.exception.CustomException;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ShoppingMallLockService {

    private final ShoppingMallRepository shoppingMallRepository;
    private final ShoppingMallStatsRepository shoppingMallStatsRepository;
    private final ShoppingMallOptimisticLockService optimisticLockService;

    public synchronized ShoppingMall getShoppingMallByIdWithSynchronized(Long id) {
        ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        stats.incrementViewCount();
        shoppingMallStatsRepository.save(stats);

        return shoppingMall;
    }

    @Transactional
    public ShoppingMall getShoppingMallByIdWithPessimisticLock(Long id) {
        ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        ShoppingMallStats stats = shoppingMallStatsRepository
            .findByShoppingMallIdWithPessimisticLock(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        stats.incrementViewCount();
        shoppingMallStatsRepository.save(stats);

        return shoppingMall;
    }

    public ShoppingMall getShoppingMallByIdWithOptimisticLock(Long id) throws InterruptedException {
        while (true) {
            try {
                return optimisticLockService.callOptimisticLock(id);
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}

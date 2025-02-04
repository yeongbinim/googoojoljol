package googoo.joljol.shopping_mall.service.lock;

import static googoo.joljol.common.exception.ExceptionType.SHOPPING_MALL_NOT_FOUND;
import static googoo.joljol.common.exception.ExceptionType.SHOPPING_MALL_STATS_NOT_FOUND;

import googoo.joljol.common.exception.CustomException;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.RedisLockRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ShoppingMallLockService {

    private final ShoppingMallRepository shoppingMallRepository;
    private final ShoppingMallStatsRepository shoppingMallStatsRepository;
    private final ShoppingMallOptimisticLockService optimisticLockService;
    private final RedisLockRepository redisLockRepository;
    private final RedissonClient redissonClient;

    public synchronized ShoppingMall getShoppingMallByIdWithSynchronized(Long id) {
        ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_STATS_NOT_FOUND));

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
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_STATS_NOT_FOUND));

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

    @Transactional
    public ShoppingMall getShoppingMallByIdWithLettuce(Long id) throws InterruptedException {
        while (!redisLockRepository.lock(id)) {
            Thread.sleep(100);
        }

        try {
            ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
                .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

            ShoppingMallStats stats = shoppingMallStatsRepository
                .findByShoppingMallId(id)
                .orElseThrow(() -> new CustomException(SHOPPING_MALL_STATS_NOT_FOUND));

            stats.incrementViewCount();
            shoppingMallStatsRepository.save(stats);

            return shoppingMall;
        } finally {
            redisLockRepository.unLock(id);
        }
    }

    @Transactional
    public ShoppingMall getShoppingMallByIdWithRedisson(Long id) throws InterruptedException {
        RLock lock = redissonClient.getLock(id.toString());

        try {
            boolean available = lock.tryLock(25, 1, TimeUnit.SECONDS);

            if (available) {
                ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
                    .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

                ShoppingMallStats stats = shoppingMallStatsRepository
                    .findByShoppingMallId(id)
                    .orElseThrow(() -> new CustomException(SHOPPING_MALL_STATS_NOT_FOUND));

                stats.incrementViewCount();
                shoppingMallStatsRepository.save(stats);

                return shoppingMall;
            }
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}

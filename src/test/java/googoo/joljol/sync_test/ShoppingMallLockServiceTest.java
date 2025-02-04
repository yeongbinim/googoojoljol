package googoo.joljol.sync_test;

import static org.assertj.core.api.Assertions.assertThat;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import googoo.joljol.shopping_mall.service.ShoppingMallLockService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

// Synchronized, 비관락, 낙관락, 네임드락, 분산락 적용하여 테스트
@SpringBootTest
@ActiveProfiles("test")
class ShoppingMallLockServiceTest {

    @Autowired
    private ShoppingMallLockService shoppingMallService;

    @Autowired
    private ShoppingMallStatsRepository shoppingMallStatsRepository;

    @Autowired
    private ShoppingMallRepository shoppingMallRepository;

    private Long shoppingMallId = 1L;

    @BeforeEach
    public void before() {
        ShoppingMall shoppingMall = new ShoppingMall("test");
        shoppingMallRepository.saveAndFlush(shoppingMall);
        shoppingMallId = shoppingMall.getId();
        ShoppingMallStats shoppingMallStats = new ShoppingMallStats();
        ReflectionTestUtils.setField(shoppingMallStats, "shoppingMall", shoppingMall);
        shoppingMallStatsRepository.saveAndFlush(shoppingMallStats);
    }

    @AfterEach
    public void after() {
        shoppingMallStatsRepository.deleteAll();
        shoppingMallRepository.deleteAll();
    }

    @Test
    void 조회수_100개_동시에_증가_Synchronized() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    shoppingMallService.getShoppingMallByIdWithSynchronized(shoppingMallId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(shoppingMallId)
            .orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(100);
    }

    @Test
    void 조회수_100개_동시에_증가_PessimisticLock() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    shoppingMallService.getShoppingMallByIdWithPessimisticLock(shoppingMallId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(shoppingMallId)
            .orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(100);
    }
}

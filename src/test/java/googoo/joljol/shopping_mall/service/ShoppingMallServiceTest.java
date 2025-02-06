package googoo.joljol.shopping_mall.service;

import googoo.joljol.JoljolApplication;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static googoo.joljol.shopping_mall.entity.QShoppingMall.shoppingMall;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class ShoppingMallServiceTest {

    @Autowired
    private ShoppingMallStatsRepository shoppingMallStatsRepository;

    @Autowired
    private ShoppingMallRepository shoppingMallRepository;
    @Autowired
    private ShoppingMallService shoppingMallService;

    @Test
    void 동시_업데이트_시_낙관적_락_테스트() throws InterruptedException {
        // 1. 테스트용 데이터 저장
        ShoppingMall shoppingMall1 = new ShoppingMall("테스트");
        shoppingMallRepository.save(shoppingMall1);

        ShoppingMallStats stats = new ShoppingMallStats();
        ReflectionTestUtils.setField(stats, "shoppingMall", shoppingMall);
        shoppingMallStatsRepository.save(stats);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);

        //  두 개의 스레드가 동시에 같은 데이터를 수정하도록 설정
        for (int i = 0; i < 100; i++) {
            executor.execute(() -> {
                try {
                    // 동일한 데이터를 조회한 후 업데이트
                    ShoppingMallStats sameStats = shoppingMallStatsRepository.findById(stats.getId()).orElseThrow();
                    sameStats.incrementViewCount();
                    shoppingMallStatsRepository.save(sameStats);
                } catch (OptimisticLockingFailureException e) {
                    System.out.println("낙관적 락 충돌 발생!");
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 작업을 마칠 때까지 대기
        executor.shutdown();

        // 4. 하나의 업데이트만 성공했는지 확인
        ShoppingMallStats updatedStats = shoppingMallStatsRepository.findById(stats.getId()).orElseThrow();
        Assertions.assertThat(updatedStats.getViewCount()).isEqualTo(100);
    }

    @Test
    void 비관적_락_테스트() throws InterruptedException {
        // 1. 테스트용 쇼핑몰 및 통계 데이터 저장
        ShoppingMall shoppingMall = new ShoppingMall("종이비행기");
        shoppingMallRepository.save(shoppingMall);

        ShoppingMallStats stats = new ShoppingMallStats();
        ReflectionTestUtils.setField(stats, "shoppingMall", shoppingMall);
        shoppingMallStatsRepository.save(stats);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executor.execute(() -> {
                try {
                    // 비관적 락을 사용하여 조회
                    shoppingMallService.getShoppingMallById(shoppingMall.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 작업을 마칠 때까지 대기
        executor.shutdown();

        // 3. 하나의 트랜잭션만 성공했는지 확인
        ShoppingMallStats updatedStats = shoppingMallStatsRepository.findById(stats.getId()).orElseThrow();
        assertEquals(100, updatedStats.getViewCount(), "비관적 락이 정상적으로 동작해야 함");
    }

}
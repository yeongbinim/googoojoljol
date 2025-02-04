package googoo.joljol.shopping_mall.repository;

import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ShoppingMallStatsRepository extends JpaRepository<ShoppingMallStats, Long>,
    ShoppingMallStatsRepositoryCustom {

    Optional<ShoppingMallStats> findByShoppingMallId(Long shoppingMallId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
          SELECT s
          FROM ShoppingMallStats s
          WHERE s.shoppingMall.id = :shoppingMallId
        """)
    Optional<ShoppingMallStats> findByShoppingMallIdWithPessimisticLock(Long shoppingMallId);


    @Query("""
          SELECT s
          FROM ShoppingMallStats s
          WHERE s.shoppingMall.id = :shoppingMallId
        """)
    Optional<ShoppingMallStats> findByShoppingMallIdWithOptimisticLock(Long shoppingMallId);
}

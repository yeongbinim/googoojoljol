package googoo.joljol.shopping_mall.repository;

import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingMallStatsRepository extends JpaRepository<ShoppingMallStats, Long> {

    Optional<ShoppingMallStats> findByShoppingMallId(Long shoppingMallId);
}

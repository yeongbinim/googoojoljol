package googoo.joljol.shopping_mall.repository;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Stream;

public interface ShoppingMallRepository extends JpaRepository<ShoppingMall, Long>, ShoppingMallRepositoryCustom {

    List<ShoppingMall> findAllByOrderByMonitoringDateDesc();

    @Query(value = "SELECT * FROM shopping_mall sm LIMIT :limit", nativeQuery = true)
    List<ShoppingMall> findAllLimit(@Param("limit") int limit);
}

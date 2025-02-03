package googoo.joljol.shopping_mall.repository;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.stream.Stream;

public interface ShoppingMallRepository extends JpaRepository<ShoppingMall, Long>, ShoppingMallRepositoryCustom {

    List<ShoppingMall> findAllByOrderByMonitoringDateDesc();
}

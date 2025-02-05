package googoo.joljol.shopping_mall.repository;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingMallRepository extends JpaRepository<ShoppingMall, Long>, ShoppingMallRepositoryCustom {

}

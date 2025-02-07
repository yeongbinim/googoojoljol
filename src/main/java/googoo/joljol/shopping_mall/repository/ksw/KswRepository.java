package googoo.joljol.shopping_mall.repository.ksw;

import org.springframework.data.jpa.repository.JpaRepository;

import googoo.joljol.shopping_mall.entity.ShoppingMall;

public interface KswRepository extends JpaRepository<ShoppingMall, Long>, KswRepositoryCustom {
}

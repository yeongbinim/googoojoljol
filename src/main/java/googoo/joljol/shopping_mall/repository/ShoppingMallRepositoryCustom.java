package googoo.joljol.shopping_mall.repository;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShoppingMallRepositoryCustom {
    Page<ShoppingMall> findByFilters(Integer overallRating, String businessStatus, Pageable pageable);
}

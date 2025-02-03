package googoo.joljol.shopping_mall.repository;

import googoo.joljol.shopping_mall.dto.ShoppingMallTop10Dto;
import java.util.List;

public interface ShoppingMallStatsRepositoryCustom {

    List<ShoppingMallTop10Dto> findTop10ByOrderByViewCountDesc();
}

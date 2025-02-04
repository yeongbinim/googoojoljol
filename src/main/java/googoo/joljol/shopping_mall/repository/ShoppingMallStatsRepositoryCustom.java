package googoo.joljol.shopping_mall.repository;

import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import java.util.List;

public interface ShoppingMallStatsRepositoryCustom {

    List<ShoppingMallResponseDto> findTop10ByOrderByViewCountDesc();
    List<ShoppingMallResponseDto> findHotShoppingMallByOrderByViewCountDesc(int count);
}

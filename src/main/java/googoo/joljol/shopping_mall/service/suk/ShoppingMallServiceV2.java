package googoo.joljol.shopping_mall.service.suk;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingMallServiceV2 {

    private final ShoppingMallRepository shoppingMallRepository;

    public Page<ShoppingMall> getFilteredShoppingMallsV2UsingIndex(Integer overallRating, String businessStatus, Pageable pageable) {
        return shoppingMallRepository.findByFiltersV2(overallRating, businessStatus, pageable);
    }
}

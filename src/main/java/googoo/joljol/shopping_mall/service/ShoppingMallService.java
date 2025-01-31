package googoo.joljol.shopping_mall.service;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingMallService {
    private final ShoppingMallRepository shoppingMallRepository;

    public Page<ShoppingMall> getFilteredShoppingMalls(Integer overallRating, String businessStatus, Pageable pageable) {
        return shoppingMallRepository.findByFilters(overallRating, businessStatus, pageable);
    }
}

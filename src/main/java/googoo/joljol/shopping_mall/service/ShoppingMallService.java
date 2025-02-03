package googoo.joljol.shopping_mall.service;

import static googoo.joljol.common.exception.ExceptionType.SHOPPING_MALL_NOT_FOUND;

import googoo.joljol.common.exception.CustomException;
import googoo.joljol.shopping_mall.dto.ShoppingMallTop10Dto;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import java.util.List;
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
    private final ShoppingMallStatsRepository shoppingMallStatsRepository;

    public Page<ShoppingMall> getFilteredShoppingMalls(Integer overallRating, String businessStatus,
        Pageable pageable) {
        return shoppingMallRepository.findByFilters(overallRating, businessStatus, pageable);
    }

    @Transactional
    public ShoppingMall getShoppingMallById(Long id) {
        ShoppingMall shoppingMall = shoppingMallRepository.findById(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(id)
            .orElseThrow(() -> new CustomException(SHOPPING_MALL_NOT_FOUND));

        stats.incrementViewCount();
        shoppingMallStatsRepository.save(stats);

        return shoppingMall;
    }

    public List<ShoppingMallTop10Dto> getTop10ShoppingMalls() {
        return shoppingMallStatsRepository.findTop10ByOrderByViewCountDesc();
    }
}

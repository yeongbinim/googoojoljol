package googoo.joljol.shopping_mall.service;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
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
            .orElseThrow(() -> new IllegalArgumentException("쇼핑몰을 찾을 수 없습니다."));

        ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(id)
            .orElseThrow(() -> new IllegalArgumentException("쇼핑몰 조회수 데이터를 찾을 수 없습니다."));

        stats.incrementViewCount();
        shoppingMallStatsRepository.save(stats);

        return shoppingMall;
    }
}

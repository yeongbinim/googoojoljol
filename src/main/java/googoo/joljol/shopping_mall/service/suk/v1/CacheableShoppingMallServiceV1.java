package googoo.joljol.shopping_mall.service.suk.v1;

import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.dto.ShoppingMallTop10Dto;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CacheableShoppingMallServiceV1 {

    private final int RANKING_NUM = 100;
    private final ShoppingMallStatsRepository shoppingMallStatsRepository;

    // V1: `@Cacheable`을 이용해 쇼핑몰 랭킹 캐싱
    @Cacheable(value = "top100Malls:list", unless = "#result == null or #result.isEmpty()")
    public List<ShoppingMallResponseDto> getTopShoppingMallsByCacheable() { // 뭔가 로직이 아쉽구만. @Cacheable 데이터 조작이 안 된다.
        return shoppingMallStatsRepository.findHotShoppingMallByOrderByViewCountDesc(RANKING_NUM);
    }

}

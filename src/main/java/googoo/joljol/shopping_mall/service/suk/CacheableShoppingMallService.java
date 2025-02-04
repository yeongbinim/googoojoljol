package googoo.joljol.shopping_mall.service.suk;

import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.repository.ShoppingMallStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CacheableShoppingMallService {

    private final ShoppingMallStatsRepository shoppingMallStatsRepository;

    @Cacheable(value = "top100Malls" , unless = "#result == null or #result.isEmpty()")
    public List<ShoppingMallResponseDto> getTop100ShoppingMalls() {
        return shoppingMallStatsRepository.findHotShoppingMallByOrderByViewCountDesc(100);
    }

}

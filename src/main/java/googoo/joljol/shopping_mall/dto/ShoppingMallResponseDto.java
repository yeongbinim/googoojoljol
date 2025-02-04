package googoo.joljol.shopping_mall.dto;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;

public record ShoppingMallResponseDto(
        Long id,
        String name,
        String mallName,
        String domain,
        String phone,
        String businessStatus,
        Integer overallRating,
        Integer viewCount
) {

    public static ShoppingMallResponseDto from(ShoppingMall shoppingMall, ShoppingMallStats stats) {
        return new ShoppingMallResponseDto(
                shoppingMall.getId(),
                shoppingMall.getName(),
                shoppingMall.getMallName(),
                shoppingMall.getDomain(),
                shoppingMall.getPhone(),
                shoppingMall.getBusinessStatus(),
                shoppingMall.getOverallRating(),
                stats.getViewCount()
        );
    }
}

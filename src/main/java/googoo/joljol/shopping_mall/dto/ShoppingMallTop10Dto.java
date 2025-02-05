package googoo.joljol.shopping_mall.dto;

public record ShoppingMallTop10Dto(
        Long id,
        String name,
        String mallName,
        String domain,
        String phone,
        String businessStatus,
        Integer overallRating,
        Integer viewCount
) {

}
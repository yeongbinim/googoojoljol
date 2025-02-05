package googoo.joljol.shopping_mall.dto;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMallStats;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ShoppingMallResponseDto {

    private Long id;
    private String name;
    private String mallName;
    private String domain;
    private String phone;
    private String businessStatus;
    private Integer overallRating;
    private Integer viewCount;

    @Builder
    public ShoppingMallResponseDto(Long id, String name, String mallName, String domain, String phone,
                                   String businessStatus, Integer overallRating, Integer viewCount) {
        this.id = id;
        this.name = name;
        this.mallName = mallName;
        this.domain = domain;
        this.phone = phone;
        this.businessStatus = businessStatus;
        this.overallRating = overallRating;
        this.viewCount = viewCount;
    }

    public static ShoppingMallResponseDto from(ShoppingMall shoppingMall, ShoppingMallStats stats) {
        return ShoppingMallResponseDto.builder()
                .id(shoppingMall.getId())
                .name(shoppingMall.getName())
                .mallName(shoppingMall.getMallName())
                .domain(shoppingMall.getDomain())
                .phone(shoppingMall.getPhone())
                .businessStatus(shoppingMall.getBusinessStatus())
                .overallRating(shoppingMall.getOverallRating())
                .viewCount(stats.getViewCount())
                .build();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}

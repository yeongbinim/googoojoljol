package googoo.joljol.shopping_mall.entity;

import com.redislabs.lettusearch.Document;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("shopping_mall")
public class ShoppingMallForRedis {

    @Id
    private Long id;  // Redis의 key가 됨

    // 자주 검색되는 필드만 선택적으로 포함
    private String name;
    private String mallName;
    private String domain;
    private String businessStatus;
    private Integer overallRating;
    private LocalDate monitoringDate;

    public ShoppingMallForRedis(ShoppingMall shoppingMall) {
        this.id = shoppingMall.getId();
        this.name = shoppingMall.getName();
        this.mallName = shoppingMall.getMallName();
        this.domain = shoppingMall.getDomain();
        this.businessStatus = shoppingMall.getBusinessStatus();
        this.overallRating = shoppingMall.getOverallRating();
        this.monitoringDate = shoppingMall.getMonitoringDate();
    }

    public static ShoppingMallForRedis convertToShoppingMallForRedis(Document<String, String> result) {
        return new ShoppingMallForRedis(
                Long.parseLong(result.getId()), // Redis key
                result.get("name"),
                result.get("mallName"),
                result.get("domain"),
                result.get("businessStatus"),
                Integer.parseInt(result.get("overallRating")),
                LocalDate.parse(result.get("monitoringDate"))
        );
    }
}

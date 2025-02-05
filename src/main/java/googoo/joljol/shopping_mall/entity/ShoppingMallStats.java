package googoo.joljol.shopping_mall.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shopping_mall_stats")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingMallStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "shopping_mall_id", nullable = false, unique = true)
    private ShoppingMall shoppingMall;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0; // 조회수 초기값 0

    @Version
    private Long version;

    public void incrementViewCount() {
        this.viewCount++;
    }

    public ShoppingMallStats(ShoppingMall shoppingMall, int viewCount) {
        this.shoppingMall = shoppingMall;
        this.viewCount = viewCount;
    }

}

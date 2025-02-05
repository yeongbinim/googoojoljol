package googoo.joljol.shopping_mall.repository;

import static googoo.joljol.shopping_mall.entity.QShoppingMall.shoppingMall;
import static googoo.joljol.shopping_mall.entity.QShoppingMallStats.shoppingMallStats;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.dto.ShoppingMallTop10Dto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShoppingMallStatsRepositoryImpl implements ShoppingMallStatsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ShoppingMallTop10Dto> findTop10ByOrderByViewCountDesc() {
        return queryFactory
            .select(Projections.constructor(ShoppingMallTop10Dto.class,
                shoppingMall.id,
                shoppingMall.name,
                shoppingMall.mallName,
                shoppingMall.domain,
                shoppingMall.phone,
                shoppingMall.businessStatus,
                shoppingMall.overallRating,
                shoppingMallStats.viewCount
            ))
            .from(shoppingMallStats)
            .leftJoin(shoppingMallStats.shoppingMall, shoppingMall)
            .orderBy(shoppingMallStats.viewCount.desc())
            .limit(10)
            .fetch();
    }

    @Override
    public List<ShoppingMallResponseDto> findHotShoppingMallByOrderByViewCountDesc(int count) {
        return queryFactory
                .select(Projections.constructor(ShoppingMallResponseDto.class,
                        shoppingMall.id,
                        shoppingMall.name,
                        shoppingMall.mallName,
                        shoppingMall.domain,
                        shoppingMall.phone,
                        shoppingMall.businessStatus,
                        shoppingMall.overallRating,
                        shoppingMallStats.viewCount
                ))
                .from(shoppingMallStats)
                .leftJoin(shoppingMallStats.shoppingMall, shoppingMall)
                .orderBy(shoppingMallStats.viewCount.desc())
                .limit(count)
                .fetch();
    }
}

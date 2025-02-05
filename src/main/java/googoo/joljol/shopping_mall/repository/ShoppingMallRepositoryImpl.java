package googoo.joljol.shopping_mall.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import googoo.joljol.shopping_mall.entity.QShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMall;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
public class ShoppingMallRepositoryImpl implements ShoppingMallRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QShoppingMall shoppingMall = QShoppingMall.shoppingMall;

    @Override
    public Page<ShoppingMall> findByFilters(Integer overallRating, String businessStatus, Pageable pageable) {
        List<ShoppingMall> results = queryFactory
                .selectFrom(shoppingMall)
                .where(
                        overallRating != null ? shoppingMall.overallRating.eq(overallRating) : null,
                        businessStatus != null ? shoppingMall.businessStatus.eq(businessStatus) : null
                )
                .orderBy(shoppingMall.monitoringDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(shoppingMall.count())
                .from(shoppingMall)
                .where(
                        overallRating != null ? shoppingMall.overallRating.eq(overallRating) : null,
                        businessStatus != null ? shoppingMall.businessStatus.eq(businessStatus) : null
                );

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ShoppingMall> findByFiltersV2(Integer overallRating, String businessStatus, Pageable pageable) {

        List<ShoppingMall> content = queryFactory.selectFrom(shoppingMall)
                .where(
                        overallRating != null ? shoppingMall.overallRating.eq(overallRating) : null,
                        businessStatus != null ? shoppingMall.businessStatus.containsIgnoreCase(businessStatus) : null
                )
                .orderBy(shoppingMall.monitoringDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(shoppingMall.count())
                .from(shoppingMall)
                .where(
                        overallRating != null ? shoppingMall.overallRating.eq(overallRating) : null,
                        businessStatus != null ? shoppingMall.businessStatus.containsIgnoreCase(businessStatus) : null
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}

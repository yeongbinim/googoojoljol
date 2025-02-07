package googoo.joljol.shopping_mall.repository.ksw;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import googoo.joljol.shopping_mall.entity.QShoppingMall;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KswRepositoryImpl implements KswRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QShoppingMall shoppingMall = QShoppingMall.shoppingMall;

	/**
	 * task1
	 * @param businessStatus
	 * @param companyAddress
	 * @param mainProducts
	 * @param pageable
	 * @return
	 */
	@Override
	public Page<ShoppingMall> task1(String businessStatus, String companyAddress, String mainProducts,
		Pageable pageable) {
		List<ShoppingMall> results = queryFactory
			.selectFrom(shoppingMall)
			.where(
				businessStatus != null ? shoppingMall.businessStatus.eq(businessStatus) : null,
				companyAddress != null ? shoppingMall.companyAddress.like("%" + companyAddress + "%") : null,
				mainProducts != null ? shoppingMall.mainProducts.like("%" + mainProducts + "%") : null
			)
			.orderBy(shoppingMall.monitoringDate.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(shoppingMall.count())
			.from(shoppingMall)
			.where(
				businessStatus != null ? shoppingMall.businessStatus.eq(businessStatus) : null,
				companyAddress != null ? shoppingMall.companyAddress.contains(businessStatus) : null,
				mainProducts != null ? shoppingMall.mainProducts.contains(businessStatus) : null
			);

		return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
	}

	/**
	 * task2
	 * @param businessStatus
	 * @param pageable
	 * @return
	 */
	@Override
	public Page<ShoppingMall> task2(String businessStatus, String domain,
		Pageable pageable) {
		List<ShoppingMall> results = queryFactory
			.selectFrom(shoppingMall)
			.where(
				domain != null ? shoppingMall.domain.eq(domain) : null
			)
			.orderBy(shoppingMall.monitoringDate.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(shoppingMall.count())
			.from(shoppingMall)
			.where(
				domain != null ? shoppingMall.domain.eq(businessStatus) : null
			);

		return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
	}

	@Override
	public Page<ShoppingMall> task4(Integer overallRating, String businessStatus, String mainProducts,
		Pageable pageable) {

		List<ShoppingMall> results = queryFactory
			.selectFrom(shoppingMall)
			.where(
				overallRatingEq(overallRating),
				businessStatusEq(businessStatus),
				matchMainProducts(mainProducts)
			)
			.orderBy(shoppingMall.monitoringDate.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(shoppingMall.count())
			.from(shoppingMall)
			.where(
				overallRatingEq(overallRating),
				businessStatusEq(businessStatus),
				// matchMainProducts(mainProducts)
				mainProducts != null ? shoppingMall.mainProducts.like(mainProducts + "%") : null
			);

		return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
	}

	@Override
	public Page<ShoppingMall> task5(String businessStatus, String domain, LocalDate monitoringDate, Pageable pageable) {
		List<ShoppingMall> results = queryFactory
			.selectFrom(shoppingMall)
			.where(
				businessStatus != null ? shoppingMall.businessStatus.eq(businessStatus) : null,
				domain != null ? shoppingMall.domain.eq(domain) : null,
				monitoringDate != null ? shoppingMall.monitoringDate.loe(monitoringDate) : null
			)
			.orderBy(shoppingMall.monitoringDate.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(shoppingMall.count())
			.from(shoppingMall)
			.where(
				businessStatus != null ? shoppingMall.businessStatus.eq(businessStatus) : null,
				domain != null ? shoppingMall.domain.eq(domain) : null,
				monitoringDate != null ? shoppingMall.monitoringDate.loe(monitoringDate) : null
			);

		return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
	}

	//--------------------------------- private methods ------------------------------------------
	private BooleanExpression matchMainProducts(String mainProducts) {
		return mainProducts != null ?
			Expressions.numberTemplate(
				Double.class,
				"match_against({0},{1})",
				shoppingMall.mainProducts,
				mainProducts
			).gt(0.0) : null;
	}

	private BooleanExpression overallRatingEq(Integer overallRating) {
		return overallRating != null ?
			shoppingMall.overallRating.eq(overallRating)  // 기본 eq 사용
			: null;
	}

	private BooleanExpression businessStatusEq(String businessStatus) {
		return businessStatus != null ? shoppingMall.businessStatus.eq(businessStatus) : null;
	}
}

package googoo.joljol.shopping_mall.repository.suk;

import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ShoppingMallStatsBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    // 우선은 예외처리 생략
    public void updateViewCountInBatch(List<ShoppingMallResponseDto> cache) {
        if (cache.isEmpty()) return;

        StringBuilder sql = new StringBuilder("UPDATE shopping_mall_stats SET view_count = CASE ");
        StringBuilder whereClause = new StringBuilder(" WHERE shopping_mall_id IN (");

        for (int i = 0; i < cache.size(); i++) {
            ShoppingMallResponseDto response = cache.get(i);
            sql.append(" WHEN shopping_mall_id = ").append(response.getId())
                    .append(" THEN ").append(response.getViewCount());

            whereClause.append(response.getId());
            if (i < cache.size() - 1) {
                whereClause.append(", ");
            }
        }

        sql.append(" END");
        sql.append(whereClause).append(")");

        jdbcTemplate.update(sql.toString());
    }
}

package googoo.joljol.shopping_mall.repository.ksw;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import googoo.joljol.shopping_mall.entity.ShoppingMall;

public interface KswRepositoryCustom {
    Page<ShoppingMall> task1(String businessStatus, String companyAddress, String mainProducts, Pageable pageable);
    Page<ShoppingMall> task2(String businessStatus, String domain, Pageable pageable);
    Page<ShoppingMall> task4(Integer overallRating, String businessStatus, String mainProducts, Pageable pageable);

	Page<ShoppingMall> task5(String businessStatus, String domain, LocalDate monitoringDate, Pageable pageable);

}

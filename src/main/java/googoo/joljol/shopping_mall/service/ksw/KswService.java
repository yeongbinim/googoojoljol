package googoo.joljol.shopping_mall.service.ksw;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.repository.ksw.KswRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KswService {

	private final KswRepository kswRepository;

	// task 1
	public Page<ShoppingMall> task1(String businessStatus, String companyAddress, String mainProducts, Pageable pageable) {
		return kswRepository.task1(businessStatus, companyAddress, mainProducts, pageable);
	}
	// task 2
	public Page<ShoppingMall> task2(String businessStatus, String domain, Pageable pageable) {
		return kswRepository.task2(businessStatus, domain, pageable);
	}

	// public ShoppingMall task3(String domain, String name, String operatorEmail) {
	// 	ShoppingMall newShoppingMall = ShoppingMall.create(domain, name, operatorEmail);
	// 	return kswRepository.save(newShoppingMall);
	// }

	public Page<ShoppingMall> task4(Integer overallRating, String businessStatus, String mainProducts, Pageable pageable) {
		return kswRepository.task4(overallRating, businessStatus, mainProducts, pageable);
	}

	public Page<ShoppingMall> task5(String businessStatus, String domain, LocalDate monitoringDate, Pageable pageable) {
		return kswRepository.task5(businessStatus, domain, monitoringDate, pageable);
	}
}

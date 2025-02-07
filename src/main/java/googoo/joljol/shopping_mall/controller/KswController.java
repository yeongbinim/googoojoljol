package googoo.joljol.shopping_mall.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.service.ksw.KswService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/shopping-mall")
@RequiredArgsConstructor
public class KswController {

	private final KswService kswService;

	// task1 복합 인덱스 적용 (FULL-TEXT)
	@GetMapping("/task1")
	public ResponseEntity<Page<ShoppingMall>> task1(
		@RequestParam(required = false) String businessStatus,
		@RequestParam(required = false) String companyAddress,
		@RequestParam(required = false) String mainProducts,
		@PageableDefault Pageable pageable
	) {
		Page<ShoppingMall> shoppingMallsTask1 = kswService.task1(businessStatus, companyAddress, mainProducts, pageable);
		return ResponseEntity.ok(shoppingMallsTask1);
	}

	// task2 인덱스 성능 테스트
	@GetMapping("/task2")
	public ResponseEntity<Page<ShoppingMall>> task2(
		@RequestParam(required = false) String businessStatus,
		@RequestParam(required = false) String domain,
		@PageableDefault Pageable pageable
	) {
		Page<ShoppingMall> shoppingMallsTask2 = kswService.task2(businessStatus, domain, pageable);
		return ResponseEntity.ok(shoppingMallsTask2);
	}

	// task2 인덱스 성능 테스트
	@GetMapping("/task5")
	public ResponseEntity<Page<ShoppingMall>> task5(
		@RequestParam(required = false) String businessStatus,
		@RequestParam(required = false) String domain,
		@RequestParam(required = false) LocalDate monitoringDate,
		@PageableDefault Pageable pageable
	) {
		Page<ShoppingMall> shoppingMallsTask2 = kswService.task5(businessStatus, domain, monitoringDate, pageable);
		return ResponseEntity.ok(shoppingMallsTask2);
	}

	// // task3 B-Tree vs Fractal-Tree 쓰기 성능 테스트
	// @PostMapping("/task3")
	// public ResponseEntity<ShoppingMall> task3(
	// 	@RequestParam(required = false) String domain,
	// 	@RequestParam(required = false) String name,
	// 	@RequestParam(required = false) String operatorEmail
	// ) {
	// 	ShoppingMall response = kswService.task3(domain, name, operatorEmail);
	// 	return new ResponseEntity<>(response, HttpStatus.CREATED);
	// }

	// task4 복합 인덱스 성능 테스트
	@GetMapping("/task4")
	public ResponseEntity<Page<ShoppingMall>> task4(
		@RequestParam(required = false) Integer overallRating,
		@RequestParam(required = false) String domain,
		@RequestParam(required = false) String mainProducts,
		@PageableDefault Pageable pageable
	) {
		Page<ShoppingMall> shoppingMallsTask4 = kswService.task4(overallRating, domain, mainProducts, pageable);
		return ResponseEntity.ok(shoppingMallsTask4);
	}



}

package googoo.joljol.shopping_mall.controller;

import googoo.joljol.shopping_mall.dto.ShoppingMallTop10Dto;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.service.ShoppingMallService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shopping-mall")
public class ShoppingMallController {

    private final ShoppingMallService shoppingMallService;

    @GetMapping
    public ResponseEntity<Page<ShoppingMall>> getShoppingMalls(
        @RequestParam(required = false) Integer overallRating,
        @RequestParam(required = false) String businessStatus,
        @PageableDefault Pageable pageable
    ) {
        Page<ShoppingMall> shoppingMalls = shoppingMallService.getFilteredShoppingMalls(
            overallRating, businessStatus, pageable);
        return ResponseEntity.ok(shoppingMalls);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingMall> getShoppingMall(@PathVariable Long id) {
        return ResponseEntity.ok(shoppingMallService.getShoppingMallById(id));
    }

    @GetMapping("/top10")
    public ResponseEntity<List<ShoppingMallTop10Dto>> getTop10ShoppingMalls() {
        return ResponseEntity.ok(shoppingMallService.getTop10ShoppingMalls());
    }
}

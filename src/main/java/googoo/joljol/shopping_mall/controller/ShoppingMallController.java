package googoo.joljol.shopping_mall.controller;

import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.service.ShoppingMallService;
import googoo.joljol.shopping_mall.service.suk.ShoppingMallServiceV3;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shopping-mall")
public class ShoppingMallController {

    private final ShoppingMallService shoppingMallService;
    private final ShoppingMallServiceV3 shoppingMallServiceV3;

    @GetMapping
    public ResponseEntity<Page<ShoppingMall>> getShoppingMalls(
            @RequestParam(required = false) Integer overallRating,
            @RequestParam(required = false) String businessStatus,
            @PageableDefault Pageable pageable
    ) {
        Page<ShoppingMall> shoppingMalls = shoppingMallService.getFilteredShoppingMalls(
                overallRating, businessStatus, pageable
        );
        return ResponseEntity.ok(shoppingMalls);
    }

}

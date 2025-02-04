package googoo.joljol.shopping_mall.controller;

import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.service.suk.ProxyShoppingMallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShoppingMallControllerBySuk {

    private final ProxyShoppingMallService proxyShoppingMallService;

    @GetMapping("/shopping-mall/hot")
    public ResponseEntity<List<ShoppingMallResponseDto>> getTop10ShoppingMalls(@RequestParam int top) {
        return ResponseEntity.ok(proxyShoppingMallService.getHotShoppingMalls(top));
    }

    @GetMapping("/shopping-mall/view/{id}")
    public ResponseEntity<ShoppingMall> getShoppingMall(@PathVariable Long id, @RequestParam Integer viewCount) {
        ShoppingMall response = proxyShoppingMallService.getShoppingMallById(id, viewCount);
        return ResponseEntity.ok(response);
    }

}

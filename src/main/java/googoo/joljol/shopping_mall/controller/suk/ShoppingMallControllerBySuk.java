package googoo.joljol.shopping_mall.controller.suk;

import googoo.joljol.shopping_mall.dto.ShoppingMallResponseDto;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.service.suk.v1.ProxyShoppingMallServiceV1;
import googoo.joljol.shopping_mall.service.suk.v2.ProxyShoppingMallServiceV2;
import jakarta.servlet.http.HttpServletRequest;
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

    private final ProxyShoppingMallServiceV1 proxyShoppingMallServiceV1;
    private final ProxyShoppingMallServiceV2 proxyShoppingMallServiceV2;

    // 그냥 리스트 캐싱
    @GetMapping("/shopping-mall/ranking")
    public ResponseEntity<List<ShoppingMallResponseDto>> getTopShoppingMalls(@RequestParam int top) {
        return ResponseEntity.ok(proxyShoppingMallServiceV1.getTopRankingShoppingMallsV1(top));
    }

    // SortedSet 캐싱
    @GetMapping("/shopping-mall/v2/ranking")
    public ResponseEntity<List<ShoppingMallResponseDto>> getTopShoppingMallsV2(@RequestParam int top) {
        return ResponseEntity.ok(proxyShoppingMallServiceV2.getTopRankingShoppingMallsV2(top));
    }

    @GetMapping("/shopping-mall/v1/view/{id}")
    public ResponseEntity<ShoppingMall> getShoppingMallV1(
            @PathVariable Long id, HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        ShoppingMall response = proxyShoppingMallServiceV1.getShoppingMallByIdV1(id, ip);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shopping-mall/v2/view/{id}")
    public ResponseEntity<ShoppingMall> getShoppingMallV2(
            @PathVariable Long id, HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        ShoppingMall response = proxyShoppingMallServiceV2.getShoppingMallByIdV2(id, ip);
        return ResponseEntity.ok(response);
    }

}

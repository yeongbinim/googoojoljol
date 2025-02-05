package googoo.joljol.shopping_mall.service.suk;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ViewHistoryManager {

    @Cacheable(value = "viewHistory",
            key = "#ip + '_' + #shoppingMallId",
            unless = "#result == null" // null일 경우 캐시하지 않음
    )
    public ViewHistory findViewHistory(Long shoppingMallId, String ip) {
        // 캐시에 없을 때만 실행
        return null;
    }

    @CachePut(value = "viewHistory", key = "#ip + '_' + #shoppingMallId")
    public ViewHistory saveViewHistory(Long shoppingMallId, String ip) {
        // 캐시에 저장만 하면 되므로 메서드 내용은 비워둠
        return new ViewHistory(ip, shoppingMallId, LocalDateTime.now().toString());
    }

    public record ViewHistory(String ip, Long shoppingMallId, String createdAt) {
    }
}

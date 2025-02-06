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

    /*
        * `Cache` 저장을 `@Cacheable`이 아닌 `@CachePut`으로 한 이유는 `@CachePut`은 캐시 여부와 관계없이 새로운 데이터를 덮어쓰기 때문
        * 만약 `@Cacheable`을 사용한 경우, 이미 캐시가 있는 경우 갱신이 되지 않지만 `@CachePut`을 사용하면 최신 캐시 값을 반영할 수 있음.
     */
    @CachePut(value = "viewHistory", key = "#ip + '_' + #shoppingMallId")
    public ViewHistory saveViewHistory(Long shoppingMallId, String ip) {
        // 캐시에 저장만 하면 되므로 메서드 내용은 비워둠
        return new ViewHistory(ip, shoppingMallId, LocalDateTime.now().toString());
    }

    public record ViewHistory(String ip, Long shoppingMallId, String createdAt) {
    }
}

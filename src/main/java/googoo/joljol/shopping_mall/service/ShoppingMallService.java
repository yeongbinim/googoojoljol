package googoo.joljol.shopping_mall.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redislabs.lettusearch.SearchResults;
import googoo.joljol.shopping_mall.entity.ShoppingMall;
import googoo.joljol.shopping_mall.repository.ShoppingMallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingMallService {

    private final ShoppingMallRepository shoppingMallRepository;

    public Page<ShoppingMall> getFilteredShoppingMalls(Integer overallRating, String businessStatus, Pageable pageable) {
        return shoppingMallRepository.findByFilters(overallRating, businessStatus, pageable);
    }

}

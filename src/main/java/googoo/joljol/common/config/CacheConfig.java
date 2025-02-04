package googoo.joljol.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.ArrayList;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("VVIC", // "VVIC"라는 이름의 캐시에 대해서는 6시간 TTL 적용
                        config.entryTtl(Duration.ofHours(6))) // shoppingMall 캐싱 시간은 6 hours
                .build();
    }

//    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        ArrayList<ConcurrentMapCache> caches = new ArrayList<>();
        caches.add(new ConcurrentMapCache("localCache_topMalls"));
        cacheManager.setCaches(caches);
        return cacheManager;
    }

//    @Primary
//    @Bean
//    public CacheManager compositeCacheManager(RedisCacheManager redisCacheManager, CacheManager cacheManager) {
//        CompositeCacheManager compositeCacheManager = new CompositeCacheManager(
//                redisCacheManager, // Cache chaining
//                cacheManager
//        );
//        compositeCacheManager.setFallbackToNoOpCache(true);
//        return compositeCacheManager;
//    }

}

# `Caching`을 이용한 쇼핑몰 랭킹 최적화 여정 DB부터 `Redis`까지

쇼핑몰 순위를 실시간으로 조회하는 기능을 구축하는 과정에서, DB 조회 방식부터 캐싱 전략까지 단계별 접근법을 비교하며 장단점을 분석해 보자.

## 📑 목차
- [`Caching`을 이용한 쇼핑몰 랭킹 최적화 여정 DB부터 `Redis`까지](#-caching---------------------db----redis---)
    * [V0: DB 조회](#1-v0-db-조회)
    * [2. V1-1: Spring Cache를 이용한 캐싱 전략](#2-v1-1-spring-cache를-이용한-캐싱-전략)
    * [3. V1-2: `Redis`를 이용한 캐싱 전략 1](#3-v1-2---redis-------------1)
    * [4. V1-3: `Spring-Cache`와 `Redis` 캐싱 방법](#4-v1-3---spring-cache----redis-------)
        + [<u> 4.1 `@Cacheable`을 이용한 랭킹 리스트 캐싱</u>](#-u--41---cacheable------------------u-)
        + [<u>4.2 `@Cachable`과 `@CachePut`을 조회수 어뷰징 관리</u>](#-u-42---cachable-----cacheput---------------u-)
    * [5. V1의 문제점](#5-v1-----)
    * [6. V2: `Redis`를 이용한 캐싱 전략 2](#6-v2---redis-------------2)
        + [<u>6.1 List -> Sorted Set</u>](#-u-61-list----sorted-set--u-)
        + [<u>6.2 `Sorted Set`의 문제점</u>](#-u-62--sorted-set--------u-)
        + [6.3 데이터 구조 이중화: `Sorted Set`과 `Hash`의 조합](#63--------------sorted-set----hash-----)
        + [6.4 `Redis` 동시성 문제와 해결방법](#64--redis--------------)
        + [6.5 +원자성이란(Atomicity)?](#65--------atomicity--)
        + [6.6 랭킹 리스트에 없는 쇼핑몰이 조회되면 어떻게 하지?](#66-----------------------------)
        + [6.7 10분마다 DB에 반영하는 건 좋다... 그런데 어떻게 쿼리를 날려야 할까?](#67-10----db-------------------------------)
    * [7. 캐싱이 아니라 메인 데이터베이스?](#7-------------------)


## 1. V0: DB 조회

가장 먼저 시도한 방식은 DB에서 직접 조회하는 방식이다. 이 방식은 실시간으로 정확한 데이터를 가져올 수 있다는 장점이 있지만, 트래픽이 증가할 경우 DB 부하가 심해지는 단점이 있다.

아래 결과를 보면 viewCount를 기준으로 상위 100개의 쇼핑몰 데이터를 DB에서 조회하는 것을 볼 수 있다. API 시나리오는 다음과 같다.

1. 특정 범위의 쇼핑몰 랭킹 조회 (예: 10위 ~ 100위)
2. `ORDER BY viewCount DESC` 및 `LIMIT` 100을 사용하여 상위 100개 데이터를 반환

(조회 속도: `84ms`)

<img src="https://github.com/user-attachments/assets/063ffcca-5172-4197-b908-5b0737bf7d22" width="600" height="300" alt="DB">

## 2. V1-1: `Spring Cache`를 이용한 캐싱 전략

캐싱을 활용하여 DB 부하를 줄이고 성능을 향상시키는 전략을 적용했다. 캐싱 방식은 두 가지를 사용했다. 첫 번째로 로컬 캐시인 `Spring Cache`를 사용했고, 다른 하나는 `Redis` 캐싱 방법을 사용했다.

1. 랭킹 데이터 조회 시 캐시를 먼저 확인
2. 캐시에 데이터가 없으면 DB에서 조회 후 캐시에 저장

이는 데이터의 완벽한 실시간성을 다소 포기하는 대신, 빠른 응답 속도를 확보하는 전략이다. 아래는 같은 데이터를 캐시에서 조회한 결과다:

(`Spring-Cache` 조회 속도: `6ms`)

<img src="https://github.com/user-attachments/assets/715fd51d-1a78-446d-bcc3-996bb30c3a73" width="600" height="300" alt="Spring-Cache Result">

시간을 보면, `6ms`로 DB에서 조회했을 때(`84ms`) 약 12배가량 빠른 조회가 가능함을 알 수 있다.

## 3. V1-2: `Redis`를 이용한 캐싱 전략 1

다음으로 `Redis`다. `Redis`는 `Spring-Cache`와 비교해 캐싱 측면에서 유의미한 차이점이 있다. 다음은 두 개의 간단한 차이점이다.

1. Spring Cache(Local Cache)의 특징:

* 애플리케이션 메모리에 저장되어 접근 속도가 매우 빠름
* 서버마다 독립적인 캐시를 가짐 (서버 간 데이터 불일치 가능)
* 애플리케이션 재시작 시 캐시 데이터 소실
* 메모리 제한이 있음

2. Redis(분산 캐시)의 특징:

* 네트워크 호출이 필요해서 Local Cache보다는 느림
* 여러 서버가 동일한 캐시 데이터 공유 가능
* 서버가 재시작되어도 데이터 유지
* 더 많은 데이터 저장 가능
* 자체 자료구조를 제공해서 풍부한 조작 가능

랭킹 데이터를 `Redis`로 조회한 결과다. 걸린 시간은 `16ms`로 확실히 같은 `Spring-Cache`와 같은 캐싱이지만 네트워크를 타야 하기 때문에 `Spring-Cache (8ms)`보다는 다소 느린 것을 알 수 있다.

<img src="https://github.com/user-attachments/assets/ec20ec0a-e5de-41e2-bb9f-bbaa3cb96a62" width="600" height="300" alt="Redis Result">

## 4. V1-3: `Spring-Cache`와 `Redis` 캐싱 방법

`CacheManager`를 사용하는 경우, `Spring-Cache`와 `Redis` 캐싱 설정 방법은 크게 다르지 않다. 캐싱을 하는 데 사용되는`CacheManager`를 달리 한 것뿐이다. 아래 코드는 각각의 `CacheManager` 설정 방법이다. 아래 설정에서 볼 수 있는 것처럼, 두 캐싱 방법은 TTL 설정에서 차이를 보인다.`Redis`는 `CacheManager`에서 직접 TTL을 설정할 수 있다. 하지만 현재 사용 중인 `Spring-Cache`의 기본 구현체인 `ConcurrentMapCache`는 TTL 설정을 지원하지 않지만, `Caffeine` 같은 다른 구현체를 사용하면 TTL 설정이 가능하다.

```java
    // RedisCacheManager 설정
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // 기본 캐싱 시간
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("VVIC", // "VVIC"라는 이름의 캐시에 대해서는 6시간 TTL 적용
                        config.entryTtl(Duration.ofHours(6))) // 
                .build();
    }
```

```java
    // Spring-Cache 설정
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        ArrayList<ConcurrentMapCache> caches = new ArrayList<>();
        caches.addAll(List.of(
                new ConcurrentMapCache("top100Malls:list"),
                new ConcurrentMapCache("viewHistory")
        ));
        cacheManager.setCaches(caches);
        return cacheManager;
    }
```

---

### <u> 4.1 `@Cacheable`을 이용한 랭킹 리스트 캐싱</u>

`Spring-Cache`와 `Redis`를 이용한 캐싱 방법 모두, 캐싱을 하기 위한 수단으로서 `@Cacheable`을 사용했다.

* `@Cacheable`

  * 메서드의 결과를 캐시에 저장하고, 동일한 키로 호출될 때 캐시된 결과를 반환
  * 캐시가 있으면 메서드를 실행하지 않고 캐시된 값을 바로 반환
  * 캐시가 없을 때만 메서드를 실행하고 그 결과를 캐시에 저장

아래 코드는 `@Cacheable`을 이용해 캐싱한 코드다. 해당 코드는 캐시에 데이터가 존재하면 해당 데이터를 반환하고, 만약 아무런 데이터가 없으면 `shoppingMallStatsRepository.findHotShoppingMallByOrderByViewCountDesc(RANKING_NUM)`에서 반환한 데이터를 캐싱한다. 한 가지 주의할 점은 `@Cacheable` 애노테이션이 AOP로 작동하므로, 내부 메서드 호출 시에는 실제 캐싱 데이터를 조회할 수 없다는 것이다.

```java
    @Cacheable(value = "top100Malls:list", unless = "#result == null or #result.isEmpty()")
    public List<ShoppingMallResponseDto> getTopShoppingMallsByCacheable() {
        return shoppingMallStatsRepository.findHotShoppingMallByOrderByViewCountDesc(RANKING_NUM);
    }
```

(`@Cacheable`의 `value`는 캐시의 네임스페이스 역할을 한다. 여러 곳에서 캐시를 사용할 때 이 값을 통해 각각의 캐시를 구분할 수 있다. `unless` 속성은 특정 조건에서 캐싱을 하지 않도록 설정하는데, 여기서는 결과가 null이거나 비어있는 경우에는 캐싱하지 않도록 설정했다)

(실제 캐시에 저장된 리스트)

<img width="900" alt="Image" src="https://github.com/user-attachments/assets/5fd33183-d3cd-48aa-a7fd-56bd07fe6f2d" />

---

### <u>4.2 `@Cachable`과 `@CachePut`을 조회수 어뷰징 관리</u>

현재 프로젝트에서는 특정 쇼핑몰을 조회할 때마다 `viewCount` 를 증가시키는데, 이때 여뷰징 방지를 위해 5분 이내 재조회시 카운팅을 시키지 않게끔 로직을 작성했다. 이번 프로젝트에서는 인증, 인가가 따로 구성되어 있지 않기 위해서 사용자를 IP로 구분했다. 스프링 `HttpServletRequest`에서 제공하는 ip address값과 쇼핑몰 id 값을 조합해 키값으로 활용해, 5분간 캐시하고 조회 이력이 캐시에 존재하면 조회수는 증가하지 않는다. 아래는 `Redis`에 저장된 이력 정보다.

<img width="1000" alt="view_history" src="https://github.com/user-attachments/assets/5dd9c6d7-1b72-4a5b-ab3f-ca3aa37d8074" />

viewHistory를 유지하기 위한 방법으로서 앞선 `@Cacheable`과 `@CachePut` 을 사용했다.

* `@CachePut`

  * 메서드를 항상 실행하고 그 결과를 캐시에 저장
  * 기존 캐시 데이터가 있더라도 새로운 값으로 갱신
  * 주로 데이터 갱신이 필요한 경우에 사용

아래 코드는 조회 이력을 관리하기 위한 클래스다.

```java
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

```

## 5. V1의 문제점

현재 `Spring-Cache`와 `Redis`를 이용한 캐싱 전략은 캐싱 데이터와 조회 이력만 확인하고, 문제가 없으면 DB에 `viewCount` 값을 업데이트하고 있다. 하지만 이 로직에는 동시성 문제가 존재한다. 예를 들어, 서로 다른 유저가 비슷한 시점에 같은 쇼핑몰을 조회하면 `viewCount` 업데이트가 누락될 수 있다.

처음에는 Lock을 걸어서 이 문제를 해결하려 했지만, Lock 메커니즘에 대한 경험이 부족했다. 대신 다른 접근 방식을 고려해보았다:

* 캐시에서는 최신 데이터를 유지
* 10분마다 한 번씩 누적된 업데이트 내역을 DB에 반영
* 이를 통해 실시간성과 데이터 정합성의 균형을 맞추고자 함

다만, 이 방식에서도 메모리에 저장되는 조회수에 대한 동시성 문제는 여전히 존재한다. (다행이 `Redis`에서 원자성(Atomic)을 보장하는 기능을 제공해 해당 기능을 사용했다)

하지만 여전히 한 가지 문제가 남아있다. 바로 캐시에 저장된 리스트를 최신화할 때 발생하는 오버헤드다. 현재 데이터가 리스트 형태로 저장되어 있어서, 조회수 순서가 바뀌거나 데이터가 삭제될 때마다 해당 위치 이후의 모든 데이터를 이동시켜야 하는 비효율이 발생한다 (시간 복잡도: `O(N)`)

## 6. V2: `Redis`를 이용한 캐싱 전략 2

### <u>6.1 List -> Sorted Set</u>

V1에서는 리스트를 최신화할 때 데이터 변경된 지점을 기점으로 이후의 데이터를 이동시켜야 하는 비효율이 발생했다. 그래서 `Redis`에서 제공하는 `Sorted Set(ZSet)`을 사용했다. Sorted Set은 다음과 같은 장점이 있다:

1. 자동 정렬
   * 데이터 입력시 score를 기준으로 자동 정렬
   * 조회수를 score로 사용하여 별도의 정렬 작업 불필요
2. 효율적인 데이터 관리
   * 데이터 추가/삭제 시 O(log N)의 시간복잡도
   * 순위 기반 조회도 O(log N)으로 가능
3. 중복 데이터 방지
   * Set의 특성을 활용해 자연스럽게 중복 제거
   * 동일한 쇼핑몰에 대한 중복 등록 걱정 없음

`Sorted Set`의 각 엔트리는 고유한 멤버(member)와 연관된 점수(score)로 구성된다. score는 순서를 결정하는 기준값이 되며, 이를 통해 실시간으로 순위를 관리할 수 있다.

아래 이미지는 실제 Redis에 저장된 Sorted Set의 모습이다:

<img width="1000" alt="Image" src="https://github.com/user-attachments/assets/61b0f575-938f-4b28-8aca-3a56eeb0d554"/>

---

### <u>6.2 `Sorted Set`의 문제점</u>

`Sorted Set`은 `List`에 비해 정렬 오버헤드가 감소한다는 장점이 있지만, `Member`의 특성으로 인한 제한사항이 있었다. `Sorted Set`의 `Member`는 `Redis`에서 고유한 식별자 역할을 하는데, 여기에 객체를 직접 저장하면 두 가지 문제가 발생한다:

1. 검색 효율성 저하

* `Member`를 통한 조회는 O(1)이어야 하지만, 쇼핑몰 정보가 담겨 있는 객체를 `Member`로 사용하면 이 이점을 활용할 수 없음
* 특정 데이터를 찾기 위해서는 전체 Set을 순회해야 함

2. 데이터 갱신의 어려움

* 랭킹 시스템 특성상 순위 변동이 빈번하게 발생
* 객체를 `Member`로 사용하면 데이터 갱신 시 전체 Set을 검색해야 하므로 결과적으로 `List`를 사용할 때와 비슷한 성능 문제 발생

---

### 6.3 데이터 구조 이중화: `Sorted Set`과 `Hash`의 조합

위와 같은 문제를 해결하기 위해 V2에서는 랭킹 데이터 구조를 이중화했다. 구체적으로 다음과 같은 방식을 사용했다:

1. `Sorted Set` 구조
   * Member: 쇼핑몰 ID (고유 식별자)
   * Score: 조회수(viewCount)
2. `Hash` 구조
   * 쇼핑몰의 상세 정보를 별도로 저장
   * Key-Value 형태로 빠른 접근 가능

이렇게 두 자료구조를 조합함으로써 각각의 장점을 활용할 수 있었다:

* Sorted Set으로 효율적인 랭킹 관리 (자동 정렬)
* Hash로 빠른 데이터 접근 (O(1) 시간복잡도)

위와 같이 랭킹 데이터 구조를 이중화함으로써 랭킹 시스템의 성능을 최적화할 수 있었다. 캐시에 저장된 `Sorted Set`과 `Hash`로 이루어진 랭킹 데이터는 아래와 같다.

<img width="1000" alt="Image" src="https://github.com/user-attachments/assets/95cc3677-35d3-49b8-b66d-43dcb1e9c5ef" />

<img width="1000" alt="Image" src="https://github.com/user-attachments/assets/7c5a2f7c-af42-4998-853c-5155767e5479" />

---

### 6.4 `Redis` 동시성 문제와 해결방법

V2에서는 Redis에 저장된 캐시 데이터를 최신 상태로 간주한다. 하지만 여기서 중요한 동시성 문제가 발생할 수 있다.예를 들어, UserA와 UserB가 동시에 같은 쇼핑몰을 조회하는 상황을 생각해보자. 두 사용자의 요청이 거의 동시에 처리되면서 Redis의 조회수를 업데이트할 때, UserB의 업데이트가 UserA의 변경 사항을 덮어쓸 수 있는 레이스 컨디션(Race Condition)이 발생할 수 있다.

다행히도 Redis는 이러한 동시성 문제를 해결하기 위한 몇 가지 기능을 제공한다. 특히, Redis는 기본적으로 싱글 스레드로 동작하기 때문에, 한 번에 하나의 명령만 실행된다. 덕분에 INCR 같은 연산은 원자적(Atomic) 으로 실행되므로, 별도의 락이나 트랜잭션 없이도 안전하게 동시성 문제를 처리할 수 있다.

사실, Redis는 싱글 스레드 기반의 메모리 DB이기 때문에, 단일 연산에 대해서는 원자성이 보장된다. 따라서 INCR, ZINCRBY 같은 연산은 별도의 동기화 없이도 동시성 문제가 발생하지 않는다. 그러나 여러 개의 명령어(GET → SET)를 조합해서 실행할 경우, 레이스 컨디션이 발생할 수 있으므로 주의가 필요하다.

아래 코드는 Sorted Set과 Hash에 저장된 viewCount를 업데이트하는 로직이다. 조회수를 업데이트할 때는 다음 두 개의 메서드가 호출된다.

* updateZSetRanking(): viewCount를 Sorted Set의 Score 값으로 활용하며, 이를 업데이트한다.
* updateHashRanking(): Hash에 저장된 viewCount 값을 업데이트한다.

```java
    private void updateZSetRanking(Long shoppingMallId) {
        zSetOps.incrementScore(ZSET_CACHE_KEY, shoppingMallId.toString(), 1); // atomic 
    }


    private void updateHashRanking(Long shoppingMallId) {

        Object obj = hashOps.get(HASH_CACHE_KEY, shoppingMallId.toString());

        ShoppingMallResponseDto response = objectMapper.convertValue(obj, ShoppingMallResponseDto.class);
        response.increaseViewCount();

        if (obj != null) {
            hashOps.put(HASH_CACHE_KEY, response.getId().toString(), response);
        }
    }
```

현재 구조에서는 각 연산이 개별적으로는 Thread-safe하지만, 두 개의 자료구조를 동시에 업데이트할 때는 트랜잭션 처리가 필요하다.

<u>**트랜잭션 미적용에 대한 고려**</u>

1. Sorted Set의 Score 값은 원자적으로(Atomic) 증가하므로 정확성이 보장된다.
2. Hash에 저장된 viewCount는 Race Condition이 발생할 가능성이 있지만, 조회수를 노출할 때 Sorted Set의 Score 값을 기준으로 표시하므로 큰 문제가 되지 않는다.
3. DB에 업데이트할 때는 Sorted Set의 Score 값과 Hash의 viewCount 값이 다를 경우, Score 값을 우선하도록 설정했다.

현재 구조는 동시성 문제가 완전히 해결되지 않은 상태이므로, 추후 트랜잭션을 적용하는 것이 바람직하다.

<추후 개선 방향>

* `MULTI/EXEC` 트랜잭션 적용: `Sorted Set`과 `Hash`를 동시에 업데이트할 때 일관성을 유지하도록 개선
* `Redis`의 WATCH 사용: `viewCount`가 변경되었는지 감지하고, Race Condition을 방지하는 Optimistic Locking 적용
* `Redisson` 분산 락 활용: 높은 동시성을 요구하는 환경에서 안전한 업데이트 보장

---

### 6.5 +원자성이란(Atomicity)?

원자성(Atomicity)이란 작업이 문자 그대로 더이상 나눌 수 없는 단위로 실행되는 성질을 의미한다. 즉, 작업이 실행되면 전부 실행되거나, 하나도 실행되지 않는다. 이를 통해 중간에 변경 사항이 손실되거나, 연산이 일부만 적용되는 문제를 해결할 수 있다. DB에 데이터를 저장하거나 변경을 가할 때 비즈니스 로직을 'Transaction'이라는 하나의 단위로 묶는 것 역시 연산의 원자성을 지키기 위함이다.

`Redis`에서도`GET`과 `SET` 같은 명령어들이 연달아서 실행될 때는 원자성이 깨질 수 있다. `Redis`는 싱글 스레드 모델이기에 한 번에 하나의 명령어를 처리할 때는 원자성이 지켜지지만, 특정 비즈니스 로직의 원자성은 보장되지 않기 때문이다. 그렇기 때문에 `Redis`에서도 트랜잭션 기능을 지원한다.

정리하자면, `Redis`는 싱글 스레드라서 각 개별 명령어(INCR, ZINCRBY)는 원자적이지만 `GET`후 `SET`처럼 '두 개 이상의 명령를 조합해서 실행'하면 `Race Condition` 문제가 발생할 수 있다. 여러 명령 조합의 원자성을 보장하기 위해서 `Redis`에서는 트랜잭션 기능을 지원한다.

---

### 6.6 랭킹 리스트에 없는 쇼핑몰이 조회되면 어떻게 하지?

V2에서는 조회수 기반 랭킹 처리에서 중요한 엣지 케이스를 발견했다. 시스템은 상위 100개 쇼핑몰의 데이터를 최신 상태로 유지하고 10분마다 DB에 변경 내역을 반영하는 구조인데, 처음에는 단순히 랭킹 순위 변동만 고려했다. 하지만 두 가지 중요한 시나리오를 놓치고 있었다.

1. 새로운 쇼핑몰이 랭킹권에 진입하는 경우
2. 기존 쇼핑몰이 랭킹권 밖으로 밀려나는 경우

특히 랭킹권 밖으로 밀려난 쇼핑몰을 캐시에서 삭제하면, 해당 쇼핑몰의 이후 조회수 변동을 추적할 수 없게 된다. 또한 아직 랭킹권에 진입하지 못한 새로운 쇼핑몰의 조회수도 지속적으로 추적해야 한다. 이러한 문제를 해결하기 위해, 캐시 데이터 사이즈를 100개로 제한하지 않고 10분 동안의 모든 조회수 변동을 기록하도록 수정했다. 이렇게 수집된 전체 데이터는 주기적으로 DB에 일괄 반영된다.

---

### 6.7 10분마다 DB에 반영하는 건 좋다... 그런데 어떻게 쿼리를 날려야 할까?

10분마다 DB에 변경사항을 반영할 때 중요한 문제가 있다. Redis에서 관리되던 각 쇼핑몰의 viewCount를 어떻게 효율적으로 DB에 업데이트할 것인가? 만약 캐시에 저장된 데이터가 500개라면 500번의 쿼리를 날리는 것은 너무 비효율적이었다. 즉 `Update shopping_mall_stats SET view_count = ? WHERE shopping_mall_id in (123, 222, 312 ...)` <- 이런 식으로 한 번에 쿼리를 날려서 업데이트하고 싶지만, `viewCount` 값이 모두 달라서 일괄적으로 처리가 번거로운 상황이다.

현재 상황:

1. 각 쇼핑몰마다 다른 viewCount 값을 가짐
2. 단순 `JPA update`로는 벌크 업데이트 처리가 비효율적
3. JPQL로 시도했으나 여러 레코드의 서로 다른 값을 한 번에 업데이트하기 까다로움

여러번 나눠서 처리하는 게 아니라 한 번에 처리하고 싶기에, Native Query를 사용했다. `view_count`와 `shopping_mall_id`를 매칭시켜야 하기에, `CASE-WHEN` 구문을 활용했다. 아래는 실제 DB로 날라간 쿼리의 일부다:

```sql
UPDATE
        shopping_mall_stats 
    SET
        view_count = CASE  
            WHEN shopping_mall_id = 124732 
                THEN 12312 
            WHEN shopping_mall_id = 132984 
                THEN 3333 
            WHEN shopping_mall_id = 124389 
                THEN 2222 
         .
         . 
         . 
         END
    WHERE shopping_mall_id IN (124732, 132984, 124389 ...)
  
```

이렇게 처리하면 변경해야 할 데이터가 500개라도 500번의 쿼리가 아닌 단 한 번의 쿼리로 모든 변경사항을 반영할 수 있어 성능상 이점이 크다. DB 부하도 줄이고 전체 처리 시간도 단축할 수 있다.

## 7. 캐싱이 아니라 메인 데이터베이스?

V2를 진행하면서 랭킹 정보의 최신 내역이 RDBMS가 아닌 Redis에 존재한다는 점을 인식하게 되었다. 전통적인 구조에서는 RDBMS가 메인 데이터베이스, Redis가 보조적인 캐시 역할을 수행하지만, 이번 프로젝트에서는 실제 운영 데이터가 Redis에 저장되고, RDBMS는 주기적으로 동기화되는 백업 용도로 사용되고 있다. 그러면 Redis를 **캐시(Cache)**라고 부르는 것이 적절한 표현일까? 사실상 Redis가 메인 DB 역할을 수행하고 있으며, RDBMS는 데이터 보존 및 복구를 위한 저장소에 가까운 개념이 된다.

즉 V2에서 RDMS는 보조적인 백업 및 동기화 저장소로 사용되고 있기에 `Redis`를 캐시라고 부르는 것은 적절하지 않을 수 있다. 그렇기에 `Redis`를 메인 데이터베이스로 사용하고 있다면 향후 생각해야 할 부분은 `Redis`의 가용성을 확보하는 일이다. 메모리에 저장되기에 서버가 다운되면 모든 데이터가 휘발될 수 있다. 그렇기에 AOF나 스냅샷 기능을 이용해 어느 정도 백업을 해두거나 보다 메모리 데이터베이스의 고가용성을 확보하기 위해 센티넬이나 클러스터 기능을 사용해도 좋을 듯하다.

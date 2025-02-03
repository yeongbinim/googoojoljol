# 비동기 데이터 삽입 속도, 동기 방식보다 얼마나 빠를까? (feat. `@Async`)

약 13만 개가량의 테스트 데이터를 조회할 때 발생하는 시간을 줄이고자, `Redis`를 이용해 메모리 캐싱 작업을 시도했다. 13만 모두를 삽입하는 게 좋은 방식은 아닐 테지만, 우선 데이터를 삽입한 후 갖고 놀기 위해서 13만 개의 데이터를 모두 메모리에 저장했다. 데이터 개수가 13만 개다 모두 메모리에 삽입하는데도 시간이 많이 걸렸다. 그래서 비동기 방식을 이용해 데이터를 삽입하니 확실히 삽입하는 데 걸리는 시간이 줄어들었다. 아래 코드는 동기/비동기 방식으로 작성한 코드다.

(스프링에서 비동기 기능을 사용하기 위해서는 `Application` 레벨 클래스에 `@EnableSync` 애노테이션을 추가해야 한다)

```java
private void insertDataIntoRedisWithZSet() {
    log.info("Redis ZSet 동기 데이터 저장 시작");
    redisTemplate.delete(CACHE_KEY);

    List<ShoppingMall> allShoppingMalls = shoppingMallRepository.findAll();
    ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

    allShoppingMalls.forEach(shoppingMall -> {
        try {
            String json = objectMapper.writeValueAsString(shoppingMall);
            double score = -shoppingMall.getMonitoringDate().toEpochDay(); // 날짜 기반 정렬

            zSetOps.add(CACHE_KEY, json, score);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패 - ID: {}, 오류: {}", shoppingMall.getId(), e.getMessage());
        }
    });

    // Redis TTL 설정 (1시간)
    redisTemplate.expire(CACHE_KEY, CACHE_TTL, TimeUnit.SECONDS);
    log.info("Redis ZSet 동기 데이터 저장 요청 완료");
}

// ------------------------------------------

// BATCH_SIZE -> 1000개, 1000개 단위로 비동기 삽입
@Async
public CompletableFuture<Void> insertDataIntoRedisWithZSetAsync() {
    log.info("Redis ZSet 비동기 데이터 저장 시작");
    redisTemplate.delete(CACHE_KEY);

    List<ShoppingMall> allShoppingMalls = shoppingMallRepository.findAll();
    ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

    // ✅ `CompletableFuture` 리스트로 모든 병렬 작업을 추적
    List<CompletableFuture<Void>> futures = IntStream.range(0, (allShoppingMalls.size() + BATCH_SIZE - 1) / BATCH_SIZE)
            .mapToObj(batchIndex -> CompletableFuture.runAsync(() -> {
                int start = batchIndex * BATCH_SIZE;
                int end = Math.min(start + BATCH_SIZE, allShoppingMalls.size());

                for (int i = start; i < end; i++) {
                    ShoppingMall mall = allShoppingMalls.get(i);
                    try {
                        String json = objectMapper.writeValueAsString(mall);
                        double score = -mall.getMonitoringDate().toEpochDay();
                        zSetOps.add(CACHE_KEY, json, score);
                    } catch (JsonProcessingException e) {
                        log.error("JSON 직렬화 실패 - ID: {}, 오류: {}", mall.getId(), e.getMessage());
                    }
                }
            }))
            .collect(Collectors.toList());

    // ✅ 모든 작업이 끝날 때까지 기다린 후 TTL 설정
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                redisTemplate.expire(CACHE_KEY, CACHE_TTL, TimeUnit.SECONDS);
                log.info("Redis ZSet 비동기 데이터 저장 완료");
            });
    }
```

**동기 방식으로 13만 개 데이터 삽입하는 데 걸린 시간 -> `1m 47s`**

![sync](https://github.com/user-attachments/assets/eaceca93-dd2b-473d-900c-81a13767fcb7)

**비동기 방식으로 13만 개 데이터 삽입하는 데 걸린 시간 -> `3.45s`**

![async](https://github.com/user-attachments/assets/597b0bbf-0bbf-48f3-96c9-d2cd30aed846)

위 결과에서도 볼 수 있다시피, 비동기 방식이 동기 방식보다 좋은 성능을 보여줬다. 물론 테스트마다 결과에 차이가 있겠지만 체감상으로도 비동기 방식이 압도적으로 삽입 속도가 빨랐다.

\+ **스프링에 대해 얕게만 알다 보니, 스프링에서 '비동기'라는 개념이 필요 없다고 생각했다. `Tomcat`이 요청이 들어오면 각 요청마다 스레드를 할당하여 병렬 처리를 해주기 때문에, 애초에 동기 방식만으로도 충분할 것이라 여겼다.즉, 내가 만든 API는 각 요청마다 별도의 스레드에서 실행되므로, 동기적으로 처리해도 성능에 큰 문제가 없을 것이라 판단했다. 그러나 위 상황처럼 IO 작업으로 메인 스레드가 블로킹된 상황이라면 해당 스레드가 자원을 점유하는 시간이 길어지기에 컴퓨팅 자원 할당 측면에서 그리 좋지는 못한 것 같다. 그렇기에 이런 경우에는 비동기 방식으로 IO 작업 속도를 높이는 게 더 나을 듯하다.**

# `Redis`를 이용한 캐싱 전략 - `ZSet`

캐싱 전략 구현을 위해 약 13만 개의 테스트 데이터를 `Redis`의 `ZSet` 자료구조에 저장했다. ZSet은 데이터 삽입 시 자동 정렬되는 장점이 있으나, 매 삽입마다 정렬 작업이 수행되어 `O(log N)`의 시간 복잡도가 발생한다.

기존 DB 기반 API는 데이터베이스 단에서 필터링을 수행한 후 애플리케이션 레벨에서 페이징 처리를 했다. 반면 `Redis`는 자체 필터링 기능이 제한적이어서, 전체 데이터를 메모리로 로드한 후 필터링과 페이징을 수행해야 했다. 이로 인해 약 13만 개의 데이터를 모두 조회한 후 처리해야 하는 비효율이 발생했고, 결과적으로 기존 DB 조회 방식보다 더 긴 응답 시간을 기록했다. 아래는 기존 DB 조회 방식과 레디스 캐싱 방식을 비교한 결과다.

**일반 DB 조회 방식: `649ms`**

![db](https://github.com/user-attachments/assets/366ad7b1-7145-40c7-8a7f-0d153e8e4812)





**레디스 캐싱을 이용한 조회 방식 `5.69s`**

![Redis Caching](https://github.com/user-attachments/assets/734421d6-14e7-41ba-ba22-d0999c1058a0)

-> 사실 비교할 필요도 없는 듯하다.

# `Redis`를 이용한 캐시 전략 - `RediSearch`

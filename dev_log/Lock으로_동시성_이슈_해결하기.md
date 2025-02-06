# Lock으로 동시성 이슈 해결하기

동시성 이슈는 '발생되었다'를 먼저 확인하기가 쉽지 않다.

그래서 우선은 동시성 이슈가 생길만한 부분을 파악해서 해당 부분을 직접 테스트해보고, 실제로 동시성 문제가 생기는지, 생긴다면 어떻게 해결할 건지 정리해보기로 했다.

동시성 이슈는 재고관리나 선착순 쿠폰 발급... 이런 개수가 한정되어있지만 그 개수가 하나라도 누락되면 안되는 중요한 상황에서 많이 관리된다고 알고있었는데,

굳이굳이 동시성 이슈가 일어날 부분을 찾아보니깐.. 현재 인기 쇼핑몰 top10 기능을 제공해주기 위해 조회수 기능을 추가했는데,

사용자가 특정 엔티티를 조회할 때마다 해당 엔티티의 조회수가 하나 증가하는 상황이기 때문에

'이 조회수가 단 하나의 오차도 없도록 하자'

하고 목표를 세워서 동시성 이슈를 다뤄봤다.

<br/>

## 목차

- [테스트 환경](#테스트-환경)
    - [동시성을 확인하기 위한 테스트 코드 작성](#동시성을-확인하기-위한-테스트-코드-작성)
    - [실행 결과 및 문제 분석](#실행-결과-및-문제-분석)
- [단일 스레드로 해결](#단일-스레드로-해결)
    - [Synchronized로 해결하기](#synchronized로-해결하기)
- [Lock(비관락, 낙관락)](#lock비관락-낙관락)
    - [Pessimistic Lock(비관락) 으로 해결하기](#pessimistic-lock비관락-으로-해결하기)
    - [Optimistic Lock(낙관락) 으로 해결하기](#optimistic-lock낙관락-으로-해결하기)
    - [비관락과 낙관락 결과 비교](#비관락과-낙관락-결과-비교)
- [분산락(Lettuce, Redisson)](#분산락lettuce-redisson)
    - [Lettuce로 해결하기](#lettuce로-해결하기)
    - [Redisson 으로 해결하기](#redisson-으로-해결하기)
    - [Lettuce, Redisson 결과 비교](#lettuce-redisson-결과-비교)
- [참고자료](#참고자료)

<br/>

## 테스트 환경

동시성 이슈를 발견하기 위해서는 포스트맨을 광클한다고 한 들 어림도 없을 것이다.

내 손보다, db처리속도가 더 빠르니깐 말이다.

따라서 이 동시성 이슈를 확인하기 위한 테스트 코드를 먼저 작성해볼 것이다.

<br/>

### 동시성을 확인하기 위한 테스트 코드 작성

```java

@Test
void 조회수_1_증가() {
    shoppingMallService.getShoppingMallById(shoppingMallId);
    ShoppingMallStats stats = repository.findByShoppingMallId(shoppingMallId).orElseThrow();
    assertThat(stats.getViewCount()).isEqualTo(1);
}
```

우선 조회수가 하나 조회할 때에는 당연히 문제가 없다.

<div align="center"><img width="480" alt="Image" src="https://github.com/user-attachments/assets/5d240eca-f0fc-402f-8ef0-d051973bd05a" /></div>

하지만 멀티스레딩 환경에서 요청을 동시에 실행하는 테스트 코드를 작성해보자

```java

@Test
void 조회수_100개_동시에_증가() throws InterruptedException {
    int threadCount = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(10); // (1)
    CountDownLatch latch = new CountDownLatch(threadCount); // (2)

    for (int i = 0; i < threadCount; i++) {
        executorService.submit(() -> { // (3)
            try {
                shoppingMallService.getShoppingMallById(shoppingMallId);
            } finally { // (4)
                latch.countDown(); // (2)
            }
        });
    }

    latch.await(); // (2)

    ShoppingMallStats stats = shoppingMallStatsRepository.findByShoppingMallId(shoppingMallId)
        .orElseThrow();
    assertThat(stats.getViewCount()).isEqualTo(100);
}
```

- **(1)** `Executors.newFixedThreadPool(10)`는 고정된 10개의 스레드를 가진 스레드 풀은 생성한다. (요청이 많아도 10개만 동작하고 나머지는
  대기)
- **(2)** `CoutDownLatch`는 100개의 요청이 모두 끝날 때까지 기다리도록 설정하는 동기화 도구로, 100번의 `latch.countDown()`이 호출될
  때까지 `latch.await()` 에서 기다린다.
- **(3)** `executorService.submit(() -> {})`는 executorService의 스레드 풀을 이용해 콜백 함수를 비동기 실행하도록 한다. (
  execute를 사용할 수도 있으며, submit은 Future 객체를 반환하여 예외처리가 가능하다는 장점이 있다.)
- **(4)** `try, finally` 를 사용하는 이유는 예외가 발생하더라도 latch.countDown을 꼭 해줘야 하기 때문이다.

<br/>

### 실행 결과 및 문제 분석

이 테스트코드를 실행한 결과는 아래와 같다.

<div align="center"><img width="600" alt="Image" src="https://github.com/user-attachments/assets/7580da41-8f6f-42aa-9d55-72246d220f14" /></div>

100개의 조회수 증가를 예상했지만 24개의 조회수만 증가한 것이다.

이유는 뭘까?

<div align="center"><img width="400" alt="Image" src="https://github.com/user-attachments/assets/1a23db6a-77d3-4d9c-b921-e42d16758972" /></div>

우리가 예상했던 그림은 위와 같았을 것이다. Thread1이 조회하고 변경이 끝나면, Thread2가 조회하고 변경하는 방식으로 말이다.

<div align="center"><img width="400" alt="Image" src="https://github.com/user-attachments/assets/1d02a54b-d87e-4bcb-afeb-59a0a0a8fcb0" /></div>

하지만 내부적으로는 위의 그림처럼 Thread1이 조회할때 같은 값을 Thread2도 조회해서 변경시 같은 값으로 변경하는 현상이 생긴 것이다.

즉, 스레드들 간의 RaceCondition이 발생한 것이다.

이 문제를 해결해보고자 한다.

<br/>

## 단일 스레드로 해결

### Synchronized로 해결하기

데이터에 하나의 스레드만 접근 가능하도록 synchrnized를 사용해보면 어떨까?

그렇다면 적어도 위와같이 race condition이 일어나지는 않을 것 같았다.

```java
public synchronized ShoppingMall getShoppingMallByIdWithSynchronized(Long id) {
    ShoppingMallStats stats = repository.findByShoppingMallId(id).orElseThrow();

    stats.incrementViewCount();
    shoppingMallStatsRepository.save(stats);

    return shoppingMall;
}
```

위와 같이 함수에 synchronized를 붙이게 되면 한 번에 하나의 스레드만 이 메서드를 실행할 수 있도록 보장 가능하다.

내부적으로 JVM이 객체에 대한 monitor lock을 획득하는 방식이다. (주의할 점은 메서드에 적용해도 객체에 락이 걸리는 것이다.)

이때 `@Transactional` 을 붙였을 때에는 동시성 문제가 다시 생겼었는데, 이건 프록시 객체가 생겨서 메서드가 재정의되는 문제로 생긴 부분이었다.

```java
public 새로생긴프록시객체의함수() {
    beginTransaction();
    getShoppingMallByIdWithSynchronized();
    commitTransaction();
}
```

위의 의사코드처럼 아무리 `getShoppingMallByIdWithSynchronized`가 하나의 스레드 접근을 보장한다 해도, endTransaction을 하기 직전에 다른
스레드가 접근을 한다면 동시성 문제가 똑같이 발생하는 것이다.

따라서 저 `@Transactional` 을 지워서 트랜잭셔널 보장없이 테스트를 진행해 보았다.

<div align="center"><img width="400" alt="Image" src="https://github.com/user-attachments/assets/e028c606-81db-4614-bafa-16183e9cafac" /></div>

예상대로 테스트 케이스를 통과한 것을 확인할 수 있었다. (통과가 안될 수가 없었지!)

하지만, 문제가 있었다.

트래픽이 많아질수록 병렬 처리의 장점이 사라져서 성능이 낮아진다는 점이었고,

synchrnozied는 하나의 프로세스에서만 보장이 되기때문에, 여러개의 서버를 둔다면 분명 문제가 발생할 수 있었다.

<br/>

## Lock(비관락, 낙관락)

우선 락은 특정 행이나 테이블에 접근 제어를 걸어서 다른 트랜잭션이 아예 접근하지 못하도록 하는 방법이다.

우선 대표적으로 **비관락, 낙관락**이 있는데

**비관락**은 충돌이 자주 발생한다고 생각하여 데이터베이스 수준에서 락을 설정하는 방법이고,

**낙관락**은 충돌이 드물다고 가정하여 개발자가 로직으로써 직접 작성해주는 방식이다.

<div align="center"><img width="600" alt="Image" src="https://github.com/user-attachments/assets/0c83137d-d886-4133-a3d9-bc4512453fd4" /></div>

차이점을 gif로 직접 비교해보면 쉽게 알 수 있다.

<br/>

### Pessimistic Lock(비관락) 으로 해결하기

우선 비관락 코드를 살펴보면

```java

@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<ShoppingMallStats> findByShoppingMallId(Long shoppingMallId);
```

레포지토리 메서드위에 저 `@Lock` 애너테이션으로 락 타입을 지정할 수 있고, 쓰기 전용 락인 `PESSIMISTIC_WRITE`를 적용했다.

그리고 별다른 서비스 코드를 작성할 필요 없이, 실행하면 특정 트랜잭션이 해당 메서드를 호출했다면 커밋하기 이전까지 다른 트랜잭션은 이 메서드를 대기하게 된다.

<br/>

### Optimistic Lock(낙관락) 으로 해결하기

비관락 대비 낙관락은 개발자가 직접 처리하는 부분이 많아서 뭐가 좀 많다.

```java

@Lock(LockModeType.OPTIMISTIC)
Optional<ShoppingMallStats> findByShoppingMallId(Long shoppingMallId);
```

위와같이 `@Lock`의 타입으로 `OPTIMISTIC`으로 설정해주고,

낙관락은 업데이트 시점에 version을 비교하여 조회 시점과 수정 시점의 version이 일치하는지를 비교해야 하기 때문에 **Entity에 version필드를 추가**해주어야
한다.

```java
public class ShoppingMallStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "shopping_mall_id", nullable = false, unique = true)
    private ShoppingMall shoppingMall;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Version
    private Long version; //version 필드를 통해 낙관락 적용

    public void incrementViewCount() {
        this.viewCount++;
    }
}
```

이러고 아무런 처리도 하지 않는다면... **version 일치하지 않으면 에러 발생해서 그냥 롤백**해버리기 때문에 이전과 다를게 없다.

다른 점이라면.. 이전에는 덮어쓰기였다면, 이제는 실행취소..

그래서 서비스 코드를 아래처럼 변경해주어야 한다.

```java

@Transactional
public ShoppingMall getShoppingMallById(Long id) throws InterruptedException {
    while (true) {
        try {
            ShoppingMallStats stats = repository.findByShoppingMallId(id).orElseThrow();

            stats.incrementViewCount();
            shoppingMallStatsRepository.save(stats);

            return shoppingMall;
        } catch (Exception e) {
            Thread.sleep(50);
        }
    }
}
```

이렇게 save 요청시에 version이 다른 걸 확인 후 예외가 발생해서 `Thread.sleep(50)` 으로 잠깐 쉬었다가 다시 반복문을 타도록 하는 것이다.

하지만! 이대로 실행되면 문제가 있다.

또 `@Transactional`이 문제다. 예외가 발생하면 바로 rollback시켜버리기 때문에, 저렇게 열심히 catch를 한다고 한들.. 롤백되어 버린다.

따라서 코드를 아래처럼 분리해야 한다.

```java
public class ShoppingMallFacade {

    private final ShoppingMallOptimisticLockService optimisticLockService;

    public ShoppingMall getShoppingMallById(Long id) throws InterruptedException {
        while (true) {
            try {
                return optimisticLockService.callOptimisticLock(id);
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}

public class ShoppingMallOptimisticLockService {

    private final ShoppingMallRepository shoppingMallRepository;
    private final ShoppingMallStatsRepository shoppingMallStatsRepository;

    @Transactional
    public ShoppingMall callOptimisticLock(Long id) {
        // 이전과 코드 동일
    }

}
```

<br/>

### 비관락과 낙관락 결과 비교

이제 낙관락에 대한 처리도 완료했으니 테스트를 진행해보자

<div align="center"><img width="400" alt="Image" src="https://github.com/user-attachments/assets/cd855134-08b0-4a11-aaf1-da97d5a5bb47" /></div>

Race Condition이 많이 일어나는 상황을 유도했기 때문에, 낙관락 처리가 더 오래 걸리는 것을 확인할 수 있다.

그렇다면 웬만해서는 비관락이 더 좋은게 아니냐! 할 수 있는데,

비관락도 단점이 있다.

여러 테이블에 락을 거는 트랜잭션일 경우 DeadLock 발생 위험이 있는 것이다.

그리고 비관락을 남발한다면 성능 감소가 분명히 있을 것이기 때문에 이런 점들을 고려해서 잘 써야할 것 같다.



<br/>

## 분산락(Lettuce, Redisson)

비관락이나 낙관락 처럼 데이터베이스 자체적으로 정합성을 보장하는 것이 정말 안전한 방법일 것 같지만

DB를 수평 확장(샤딩)을 해서 DB가 여러대인 경우 어떻게 할까?

해당 DB에서는 락이 걸리지만각 노드의 DB에서는 동일한 락이 적용되지 않아 정합성이 깨질 수 있다.

Redis를 이용해 분산락을 구현한다고 할 때 크게 Lettuce와 Redisson을 이용한 방법을 생각할 수 있으며 각각 하나씩 살펴보자

<br/>

### Lettuce로 해결하기

Lettuce를 통해 어떻게 분산락을 구현할 것인지 그림으로 살펴보자

<div align="center"><img width="500" alt="Image" src="https://github.com/user-attachments/assets/c006f717-d4dd-4cf8-ab65-0a7f3998c131" /></div>

1. Thread1이 레디스의 setnx 명령어를 통해 lock을 건다.

2. Thread2도 setnx 를 통해 lock을 걸려고 하면 실패한다.
3. Thread1이 DB에 데이터를 수정한다.

<div align="center"><img width="500" alt="Image" src="https://github.com/user-attachments/assets/587067bb-312f-4424-859f-4986b2194c29" /></div>

1. Thread1이 lock을 해제한다.
2. Thread2가 다시 요청을 하면 lock이 성공적으로 걸린다.
3. Thread2가 데이터를 변경한다.

이제 이것을 코드로 구현해보자

Redis를 사용해야 하므로 아래의 의존성을 추가한다.

```java
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

기본적으로 Lettuce를 사용하기 때문에 별도의 설정이 없어도 된다.

그리고 service 계층에서 사용할 수 있도록 redisTemplate을 주입받아 RedisRepository를 만든다.

```java

@Component
@RequiredArgsConstructor
public class RedisLockRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Boolean lock(Long key) {
        return redisTemplate
            .opsForValue() // (1)
            .setIfAbsent(key.toString(), "lock", Duration.ofMillis(3_000)); // (2)
    }

    public Boolean unLock(Long key) {
        return redisTemplate.delete(key.toString());
    }
}
```

- (1) `opsForValue()`: RedisTemplate을 사용할 때, 문자열(String) 타입의 데이터를 다룰 수 있도록 제공하는 API
- (2) `setIfAbsent()`: Redis의 SET NX(Not Exists) 옵션을 사용하여, 키가 없을 때만 값을 설정하는 메서드이며, 락을 3초 후 자동해제 하도록
  TTL을 설정했다.

그리고 이 RedisRepository를 통해서 락을 걸고 해제할 Service코드를 작성해보면

```java

@Transactional
public ShoppingMall getShoppingMallByIdWithLettuce(Long id) throws InterruptedException {
    while (!redisLockRepository.lock(id)) {
        Thread.sleep(100);
    }

    try {
        // 이전과 코드 동일
    } finally {
        redisLockRepository.unLock(id);
    }
}
```

lock이 해제될까지 계속 반복적으로 확인하고, 사용할 수 있다면 lock을 걸어서 로직을 수행한 이후 락을 해제한다.

이전에 작성한 낙관락과 코드가 굉장히 유사한 것을 확인할 수 있다.

이걸 spin lock 방식이라 하는데, 이런 방식은 레디스에 부하가 많이 오는 방식이긴 하다.

<br/>

### Redisson 으로 해결하기

이제 좀 더 효율적인 방식인 Redisson방식을 사용해보자

Redisson은 단순한 Redis 클라이언트가 아니라, Redis를 활용하여 고급 동시성 제어 기능을 제공하는 라이브러리인데,

pub-sub 기반의 구현이기 때문에 레디스 부하를 줄여준다는 장점을 가진다.

그림으로 한 번 확인해보자

<div align="center"><img width="500" alt="Image" src="https://github.com/user-attachments/assets/983d6832-95de-4130-916c-cbfee2aaecc3" /></div>

DB를 수정 중인 Thread1이 publisher로써 Redis 채널에 끝난 것을 알리면,

Thread2가 subscriber로써 끝난 것을 확인하여 DB 수정 작업을 진행하게 되는 것이다.

이것을 코드로 살펴보자

```java
implementation 'org.redisson:redisson-spring-boot-starter:3.44.0'
```

아쉽게도 redisson은 별도의 라이브러리를 추가해야 한다.

그러면 redissonClient를 빈주입 받을 수 있으며, 아래와 같이 코드를 마저 작성하면 된다.

```java

@Transactional
public ShoppingMall getShoppingMallByIdWithRedisson(Long id) throws InterruptedException {
    RLock lock = redissonClient.getLock(id.toString()); // (1)

    try {
        boolean available = lock.tryLock(25, 3, TimeUnit.SECONDS); // (2)

        if (available) {
            // 이전과 코드 동일
        }
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        lock.unlock(); // (3)
    }
}
```

- (1) `RLock lock = redissonClient.getLock()`: Redisson을 사용한 분산 락 획득
- (2) `lock.tryLock()`: 락을 시도하는데, 최대 25초까지 대기하고, 락을 획득하면 3초 후에는 자동 해제 되도록 설정
- (3) `lock.unlock()`: 락 해제

별도의 라이브러리를 사용하는게 아쉽지만, Lettuce대비 효율적이라고 생각할 수 있는 것은 레디스의 부하를 덜어줄 수 있다는 점인거 같다.

### Lettuce, Redisson 결과 비교

결과를 확인해보면 아래와 같다.

<div align="center"><img width="500" alt="Image" src="https://github.com/user-attachments/assets/68d43dab-b953-45cb-bd0b-1cb46b818fe7" /></div>

Lettuce는 100ms마다 락 획득을 시도하지만, Redisson은 락 해제가 되었을 때 한 번만 시도하기 때문에 조금 더 효율적인 것을 확인할 수 있다.

## 참고자료

아래는 참고자료!

[Concurrency Issues(동시성 문제) 시리즈](https://velog.io/@guns95/series/Concurrency-Issues%EB%8F%99%EC%8B%9C%EC%84%B1-%EB%AC%B8%EC%A0%9C)

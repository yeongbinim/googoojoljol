import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CompletableFutureAsyncTest {

    @Test
    void runAsyncTest() {
        CompletableFuture.runAsync(() -> {
            sleep(2000); // 2초 동안 실행 (비동기 작업)
            System.out.println("✅ 비동기 작업 완료!");
        });

        System.out.println("🚀 메인 스레드 종료!");
        // 메인 스레드가 종료되면 runAsync()의 비동기 작업이 실행되지 않을 수도 있음
    }

    @Test
    void anyOfTest() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            sleep(3000);
            return "🍎 Apple";
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            sleep(1000);
            return "🍌 Banana";
        });

        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            sleep(2000);
            return "🍇 Grape";
        });

        CompletableFuture<Object> firstCompleted = CompletableFuture.anyOf(future1, future2, future3);
        System.out.println("🏆 가장 먼저 완료된 과일: " + firstCompleted.get());
    }

    @Test
    void allOfTest() {
        List<CompletableFuture<String>> futures = List.of(
                CompletableFuture.supplyAsync(() -> "🍎 Apple"),
                CompletableFuture.supplyAsync(() -> "🍌 Banana"),
                CompletableFuture.supplyAsync(() -> "🍇 Grape")
        );

        CompletableFuture<List<String>> result = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));

        List<String> results = result.join(); // `join()`을 호출해줘야 개별 비동기 작업들 결과 반환. allOf() 자체는 모두 완료될 때까지 기다리는 게 맞음
        System.out.println("✅ 모든 결과: " + results);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}

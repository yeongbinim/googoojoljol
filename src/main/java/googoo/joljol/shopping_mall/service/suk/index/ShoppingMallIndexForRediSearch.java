package googoo.joljol.shopping_mall.service.suk.index;

import com.redislabs.lettusearch.CreateOptions;
import com.redislabs.lettusearch.Field;
import com.redislabs.lettusearch.RediSearchCommands;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/*
    * 🪆`RediSearch`는 인덱스를 설정해줘야만 검색 가능 🪆
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShoppingMallIndexForRediSearch implements CommandLineRunner {

    private final StatefulRediSearchConnection<String, String> searchConnection;
    public static final String INDEX_NAME = "shopping_mall_index";

    @Override
    public void run(String... args) {
        RediSearchCommands<String, String> commands = searchConnection.sync();

        try {
            commands.ftInfo("shopping_mall_index");
            log.info("✅ 인덱스가 이미 존재합니다.");
        } catch (Exception e) {
            log.info("🚀 인덱스 생성 중...");

            commands.create(
                    INDEX_NAME,
                    CreateOptions.<String, String>builder().prefix("shopping_mall:").build(),
                    Field.numeric("monitoringDate").sortable(true).build(),
                    Field.text("businessStatus").build(),
                    Field.numeric("overallRating").sortable(true).build()
//                    Field.text("overallRating").sortable(true).build()
            );

            log.info("✅ RediSearch 인덱스 생성 완료!");
        }
    }
}

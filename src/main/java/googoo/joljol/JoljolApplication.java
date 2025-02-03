package googoo.joljol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // 비동기 동작 활성화
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class JoljolApplication {

    public static void main(String[] args) {
        SpringApplication.run(JoljolApplication.class, args);
    }

}

package kaito.jlpt.ktjlpt;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
public class KtjlptApplication {

    public static void main(String[] args) {
        SpringApplication.run(KtjlptApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Set múi giờ chuẩn mà PostgreSQL hiểu
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        System.out.println("Spring boot application running in Asia/Ho_Chi_Minh timezone");
    }
}

package wiki.heh.bald.pay.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

/**
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@SpringBootApplication
@MapperScan("wiki.heh.bald.pay.api.mapper")
public class BaldPayApiApplication {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Do any additional configuration here
        return builder.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(BaldPayApiApplication.class, args);
    }

}

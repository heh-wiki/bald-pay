package wiki.heh.bald.pay.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class BaldPayEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaldPayEurekaApplication.class, args);
    }

}

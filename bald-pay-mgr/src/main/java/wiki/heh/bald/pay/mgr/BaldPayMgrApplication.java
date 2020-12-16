package wiki.heh.bald.pay.mgr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("wiki.heh.bald.pay.mgr.mapper")
public class BaldPayMgrApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaldPayMgrApplication.class, args);
    }

}

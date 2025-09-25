package hbnu.project.zhiyanauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyanauthservice",
        "hbnu.project.zhiyancommon"
})
@EnableFeignClients
@EnableScheduling
@EnableTransactionManagement
public class ZhiyanAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanAuthServiceApplication.class, args);
    }

}

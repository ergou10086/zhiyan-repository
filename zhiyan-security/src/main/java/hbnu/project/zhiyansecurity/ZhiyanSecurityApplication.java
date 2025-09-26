package hbnu.project.zhiyansecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "hbnu.project.zhiyansecurity",
        "hbnu.project.zhiyancommon"
})
public class ZhiyanSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanSecurityApplication.class, args);
    }

}

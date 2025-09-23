package hbnu.project.zhiyangateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 智研平台网关启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ZhiyanGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiyanGatewayApplication.class, args);
    }

}

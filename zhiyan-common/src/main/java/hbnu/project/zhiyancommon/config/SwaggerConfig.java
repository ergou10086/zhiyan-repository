package hbnu.project.zhiyancommon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger3配置类
 * 提供统一的API文档配置
 *
 * @author ErgouTree
 */
@Configuration
@ConditionalOnProperty(prefix = "swagger", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfig {

    @Autowired
    private SwaggerProperties swaggerProperties;

    /**
     * 创建OpenAPI配置
     */
    @Bean
    public OpenAPI createOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(List.of(createServer()))
                .addSecurityItem(createSecurityRequirement())
                .components(createComponents());
    }

    /**
     * 创建API信息
     */
    private Info createApiInfo() {
        return new Info()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .contact(createContact())
                .license(createLicense());
    }

    /**
     * 创建联系信息
     */
    private Contact createContact() {
        SwaggerProperties.Contact contact = swaggerProperties.getContact();
        return new Contact()
                .name(contact.getName())
                .email(contact.getEmail())
                .url(contact.getUrl());
    }

    /**
     * 创建许可证信息
     */
    private License createLicense() {
        SwaggerProperties.License license = swaggerProperties.getLicense();
        return new License()
                .name(license.getName())
                .url(license.getUrl());
    }

    /**
     * 创建服务器信息
     */
    private Server createServer() {
        SwaggerProperties.Server server = swaggerProperties.getServer();
        return new Server()
                .url(server.getUrl())
                .description(server.getDescription());
    }

    /**
     * 创建安全要求
     */
    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }

    /**
     * 创建组件配置（包括安全配置）
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", createSecurityScheme());
    }

    /**
     * 创建安全方案（JWT认证）
     */
    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("请输入JWT Token，格式为：Bearer {token}");
    }
}

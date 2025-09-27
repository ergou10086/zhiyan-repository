# Swagger API文档配置指南

## 概述

本模块为智研平台提供统一的Swagger API文档支持，包含以下功能：

- 统一的Swagger配置
- 通用的注解和工具类
- 可配置的文档信息
- JWT认证支持

## 快速开始

### 1. 添加依赖

在需要使用Swagger的微服务中，添加`zhiyan-common`依赖：

```xml
<dependency>
    <groupId>hbnu.project</groupId>
    <artifactId>zhiyan-common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

在微服务的`application.yml`中添加配置：

```yaml
swagger:
  enabled: true
  title: "认证服务API"
  description: "用户认证和权限管理"
  version: "1.0.0"
  server:
    url: "http://localhost:8081"
    description: "认证服务"

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
  packages-to-scan: hbnu.project.zhiyanauthservice
  paths-to-match: /api/**
```

### 3. 控制器注解示例

```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = SwaggerConstants.Tags.AUTH, description = "用户认证相关接口")
public class AuthController {

    @Operation(summary = "用户登录", description = "使用邮箱和密码登录系统")
    @ApiResult("登录成功")
    @PostMapping("/login")
    public R<LoginResponse> login(
            @Parameter(description = "登录请求", required = true)
            @Valid @RequestBody LoginRequest request) {
        // 实现逻辑
    }

    @Operation(summary = "用户注册", description = "注册新用户账号")
    @ApiResult("注册成功") 
    @RequirePermission // 自动添加JWT认证要求
    @PostMapping("/register")
    public R<UserDTO> register(
            @Parameter(description = "注册请求", required = true)
            @Valid @RequestBody RegisterRequest request) {
        // 实现逻辑
    }
}
```

## 核心组件

### 1. SwaggerConfig

自动配置类，基于配置文件创建OpenAPI配置。

### 2. SwaggerProperties

配置属性类，支持通过配置文件自定义API文档信息。

### 3. 注解

- `@ApiResult`: 统一响应格式注解
- `@RequirePermission`: 权限验证注解，自动添加JWT认证

### 4. 工具类

- `SwaggerUtils`: 提供常用的Swagger注解创建工具
- `SwaggerConstants`: 包含常用的标签、描述等常量

## 常用注解

### Controller级别

```java
@Tag(name = "用户管理", description = "用户信息的增删改查")
@RestController
public class UserController {
    // ...
}
```

### 方法级别

```java
@Operation(summary = "获取用户列表", description = "分页查询用户信息")
@Parameters({
    @Parameter(name = "page", description = "页码", example = "0"),
    @Parameter(name = "size", description = "每页大小", example = "10")
})
@ApiResult("查询成功")
@GetMapping("/users")
public R<Page<UserDTO>> getUsers(Pageable pageable) {
    // ...
}
```

### 参数级别

```java
@PostMapping("/users")
public R<UserDTO> createUser(
    @Parameter(description = "用户信息", required = true)
    @Valid @RequestBody CreateUserRequest request) {
    // ...
}
```

### 响应模型

```java
@Schema(description = "用户信息")
public class UserDTO {
    
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户名", example = "张三")
    private String name;
    
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;
}
```

## 访问文档

启动服务后，可以通过以下地址访问API文档：

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 环境配置

### 开发环境

```yaml
swagger:
  enabled: true
```

### 生产环境

```yaml
swagger:
  enabled: false  # 关闭文档访问
```

## 安全配置

平台已预配置JWT认证支持：

1. 在Swagger UI中点击"Authorize"按钮
2. 输入JWT Token: `Bearer your-jwt-token`
3. 即可测试需要认证的接口

## 常见问题

### 1. 文档不显示

检查以下配置：
- `swagger.enabled=true`
- 包扫描路径是否正确
- 控制器是否有`@RestController`注解

### 2. 认证失败

确保JWT Token格式正确：`Bearer <token>`

### 3. 接口分组

使用`@Tag`注解对接口进行分组：

```java
@Tag(name = SwaggerConstants.Tags.USER)
@RestController
public class UserController {
    // ...
}
```

## 最佳实践

1. **统一使用常量**: 使用`SwaggerConstants`中定义的常量
2. **完整的描述**: 为每个接口、参数、响应添加详细描述
3. **示例数据**: 在`@Schema`中添加example属性
4. **分组管理**: 使用`@Tag`对相关接口进行分组
5. **安全标记**: 对需要认证的接口使用`@RequirePermission`

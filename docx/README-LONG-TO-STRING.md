# Long ID 转 String 序列化配置说明

## 问题背景

JavaScript 中的 Number 类型只能安全表示到 `2^53-1` (即 `9007199254740991`) 的整数。当后端使用雪花ID（通常是19位的Long类型）传递给前端时，会出现精度丢失问题。

例如：
- 后端ID: `1234567890123456789`
- 前端接收: `1234567890123456768` (精度丢失)

## 解决方案

本项目提供了两种配置方式来解决这个问题：

### 1. 全局配置（推荐）

默认情况下，所有 Long 类型字段都会序列化为 String。

**配置项：**
```yaml
zhiyan:
  jackson:
    long-to-string-global: true  # 默认值，可以不配置
```

**效果：**
```json
{
  "id": "1234567890123456789",    // String 类型
  "userId": "9876543210987654321", // String 类型
  "timestamp": "1640995200000"     // String 类型
}
```

### 2. 注解配置

如果只想对特定字段进行转换，可以禁用全局配置，使用 `@LongToString` 注解。

**配置项：**
```yaml
zhiyan:
  jackson:
    long-to-string-global: false
```

**使用注解：**
```java
@Entity
public class User {
    @Id
    @LongToString  // 只有这个字段会转换为String
    private Long id;
    
    private Long version; // 这个字段保持Long类型
}
```

**效果：**
```json
{
  "id": "1234567890123456789",    // String 类型
  "version": 1                    // Number 类型
}
```

## 使用示例

### 实体类配置

```java
@Entity
@Table(name = "users")
@Data
public class User extends BaseAuditEntity {
    
    @Id
    @LongToString  // 雪花ID转String（全局配置下可省略此注解）
    private Long id;
    
    private String email;
    private String name;
}
```

### 测试接口

项目提供了测试接口来验证配置是否生效：

```bash
# 测试多个用户ID序列化
GET /api/test/snowflake-id

# 测试单个用户ID序列化  
GET /api/test/single-user
```

返回的JSON中，ID字段应该是字符串格式：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": "1234567890123456789",  // 注意：这里是字符串
    "email": "test@example.com",
    "name": "测试用户"
  }
}
```

## 技术实现

### 核心组件

1. **JacksonConfig**: Jackson配置类，提供两种ObjectMapper配置
2. **LongToStringSerializer**: 自定义Long到String序列化器
3. **@LongToString**: 注解，用于标记需要转换的字段

### 配置文件

- `zhiyan-common/src/main/resources/application.yml`: 包含配置项
- 各服务可以通过配置文件覆盖默认行为

## 注意事项

1. **数据库存储不变**: 数据库中仍然存储Long类型，只是JSON序列化时转换
2. **反序列化支持**: 前端传递String类型ID时，后端会自动转换为Long
3. **性能影响**: 序列化性能影响极小，可以忽略
4. **兼容性**: 对现有API完全兼容，不需要修改前端代码

## 常见问题

### Q: 如何验证配置是否生效？
A: 访问测试接口 `/api/test/snowflake-id`，检查返回JSON中ID字段是否为字符串格式。

### Q: 可以只对部分字段生效吗？
A: 可以，设置 `zhiyan.jackson.long-to-string-global=false`，然后使用 `@LongToString` 注解标记需要转换的字段。

### Q: 对性能有影响吗？
A: 影响极小，Long.toString() 是非常高效的操作。

### Q: 前端需要修改代码吗？
A: 不需要。JavaScript 可以直接处理字符串形式的数字，如 `parseInt("1234567890123456789")`。

## 相关文件

- `zhiyan-common/src/main/java/hbnu/project/zhiyancommon/config/JacksonConfig.java`
- `zhiyan-common/src/main/java/hbnu/project/zhiyancommon/serializer/LongToStringSerializer.java`  
- `zhiyan-common/src/main/java/hbnu/project/zhiyancommon/annotation/LongToString.java`
- `zhiyan-auth-service/src/main/java/hbnu/project/zhiyanauthservice/controller/TestController.java`

package hbnu.project.zhiyanauthservice.controller;

import hbnu.project.zhiyanauthservice.repository.UserRepository;
import hbnu.project.zhiyancommon.domain.R;
import hbnu.project.zhiyanauthservice.model.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 测试控制器
 * 用于验证雪花ID序列化配置是否生效
 *
 * @author ErgouTree
 */
@Tag(name = "测试接口", description = "用于测试Long ID序列化配置")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;

    /**
     * 测试雪花ID序列化
     * 返回的JSON中ID字段应该是String类型
     */
    @Operation(summary = "测试雪花ID序列化", description = "验证Long类型ID是否正确序列化为String")
    @GetMapping("/snowflake-id")
    public R<List<User>> testSnowflakeIdSerialization() {
        List<User> users = userRepository.findAll();
        return R.ok(users, "成功获取用户列表，请检查ID字段是否为String类型");
    }

    /**
     * 测试单个用户的ID序列化
     */
    @Operation(summary = "测试单个用户ID序列化", description = "获取第一个用户，验证ID序列化")
    @GetMapping("/single-user")
    public R<User> testSingleUserIdSerialization() {
        User user = userRepository.findAll().stream()
                .findFirst()
                .orElse(User.builder()
                        .id(1234567890123456789L)
                        .email("test@example.com")
                        .name("测试用户")
                        .build());
        return R.ok(user, "成功获取用户信息，请检查ID字段格式");
    }
}

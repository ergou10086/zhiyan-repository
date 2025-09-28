package hbnu.project.zhiyanauthservice.security;

import hbnu.project.zhiyanauthservice.service.UserService;
import hbnu.project.zhiyancommon.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 登录失败处理服务
 * 实现登录失败次数限制和账户锁定
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginFailureService {

    private final RedisService redisService;
    private final UserService userService;

    private static final String LOGIN_FAILURE_KEY_PREFIX = "login:failure:";
    private static final int MAX_FAILURE_COUNT = 5; // 最大失败次数
    private static final int LOCK_TIME_MINUTES = 30; // 锁定时间（分钟）


}

package hbnu.project.zhiyanauthservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommon.domain.R;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证失败处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    private final LoginFailureService loginFailureService;


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

    }



    private String getFailureMessage(AuthenticationException exception, String email) {
        if (exception instanceof BadCredentialsException) {

            return "邮箱或密码错误";
        } else if (exception instanceof LockedException) {
            return "账户已被锁定，请联系管理员";
        } else {
            return "登录失败，请稍后重试";
        }
    }
}

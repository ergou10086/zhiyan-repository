package hbnu.project.zhiyansecurity.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public class SecurityUtils {

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取登录用户信息
     */


    /**
     * 获取请求token
     */
    public static String getToken() {
        HttpServletRequest request = (HttpServletRequest) ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        return getToken(request);
    }

    /**
     * 根据request获取请求token
     */
    public static String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return replaceTokenPrefix(token);
    }

    /**
     * 裁剪token前缀（如Bearer）
     */
    public static String replaceTokenPrefix(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    /**
     * 是否为管理员
     */
    public static boolean isAdmin(Long userId) {
        // 根据您的业务逻辑实现
        return userId != null && userId.equals(1L); // 示例
    }

    /**
     * 密码加密
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 密码匹配
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

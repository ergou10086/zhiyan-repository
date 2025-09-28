package hbnu.project.zhiyanauthservice.security;

import hbnu.project.zhiyanauthservice.model.entity.User;
import hbnu.project.zhiyanauthservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 * 
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 重写 loadUserByUsername，根据用户邮箱加载用户详细信息
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("开始加载用户信息: {}", email);
        
        // 1. 查找用户基本信息
        Optional<User> optionalUser = userRepository.findByEmailAndIsDeletedFalse(email);
        if (optionalUser.isEmpty()) {
            log.warn("用户不存在: {}", email);
            throw new UsernameNotFoundException("用户不存在: " + email);
        }

        User user = optionalUser.get();
        
        // 2. 检查用户状态（这里不抛异常，让Spring Security的认证流程处理）
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            log.warn("用户账户已被锁定: {}", email);
        }


        // TODO：2. 加载用户权限（这是关键！）


        // TODO：3. 构建 Spring Security 需要的 UserDetails 对象
    }


    /**
     * TODO: 加载用户权限
     */
}
package hbnu.project.zhiyansecurity.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 登录用户信息上下文对象
 * 用于在应用程序中传递当前登录用户的详细信息
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserBody implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户邮箱（登录账号）
     */
    private String email;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 用户职称/职位
     */
    private String title;

    /**
     * 所属机构
     */
    private String institution;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 用户权限列表
     */
    private Set<String> permissions;

    /**
     * 是否锁定
     */
    private Boolean isLocked;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 浏览器信息
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * Token过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 判断用户是否拥有指定权限
     *
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 判断用户是否拥有指定角色
     *
     * @param role 角色名称
     * @return 是否拥有角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 判断用户是否拥有任意一个指定权限
     *
     * @param permissions 权限列表
     * @return 是否拥有任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        if (this.permissions == null || permissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (this.permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否拥有任意一个指定角色
     *
     * @param roles 角色列表
     * @return 是否拥有任意一个角色
     */
    public boolean hasAnyRole(String... roles) {
        if (this.roles == null || roles == null) {
            return false;
        }
        for (String role : roles) {
            if (this.roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否为管理员
     * 可以根据角色或其他业务逻辑判断
     *
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return hasRole("系统管理员") || hasRole("超级管理员") || hasRole("ADMIN");
    }

    /**
     * 判断账号是否可用（未锁定）
     *
     * @return 账号是否可用
     */
    public boolean isAccountNonLocked() {
        return isLocked == null || !isLocked;
    }
}


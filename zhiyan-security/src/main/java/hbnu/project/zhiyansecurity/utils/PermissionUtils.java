package hbnu.project.zhiyansecurity.utils;

import hbnu.project.zhiyansecurity.context.LoginUserBody;
import hbnu.project.zhiyansecurity.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * 权限工具类
 * 提供简化的权限检查方法
 *
 * @author ErgouTree
 */
@Slf4j
public class PermissionUtils {

    /**
     * 检查当前用户是否拥有指定权限
     */
    public static boolean hasPermission(String permission) {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.hasPermission(permission);
    }

    /**
     * 检查当前用户是否拥有指定角色
     */
    public static boolean hasRole(String role) {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.hasRole(role);
    }

    /**
     * 检查当前用户是否拥有任意一个指定权限
     */
    public static boolean hasAnyPermission(String... permissions) {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.hasAnyPermission(permissions);
    }

    /**
     * 检查当前用户是否拥有任意一个指定角色
     */
    public static boolean hasAnyRole(String... roles) {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.hasAnyRole(roles);
    }

    /**
     * 检查当前用户是否可以创建项目
     */
    public static boolean canCreateProject() {
        return hasPermission("project:create");
    }

    /**
     * 检查当前用户是否可以管理项目（基于项目ID的动态权限检查）
     * 注意：这里只检查基础权限，实际项目管理权限需要结合项目成员关系判断
     */
    public static boolean canManageProject() {
        return hasPermission("project:manage");
    }

    /**
     * 检查当前用户是否可以删除项目
     * 注意：实际删除权限需要结合项目所有者关系判断
     */
    public static boolean canDeleteProject() {
        return hasPermission("project:delete");
    }

    /**
     * 检查当前用户是否可以管理知识库
     */
    public static boolean canManageKnowledge() {
        return hasPermission("knowledge:manage");
    }

    /**
     * 检查当前用户是否为系统管理员
     */
    public static boolean isSystemAdmin() {
        return hasRole("DEVELOPER") || hasPermission("system:admin");
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前用户邮箱
     */
    public static String getCurrentUserEmail() {
        LoginUserBody loginUser = SecurityContextHolder.getLoginUser();
        return loginUser != null ? loginUser.getEmail() : null;
    }

    /**
     * 检查当前用户是否已登录
     */
    public static boolean isLoggedIn() {
        return SecurityContextHolder.isLogin();
    }
}

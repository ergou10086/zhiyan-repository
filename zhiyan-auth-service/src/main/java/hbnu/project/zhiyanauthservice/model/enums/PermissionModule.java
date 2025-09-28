package hbnu.project.zhiyanauthservice.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限模块枚举
 * 将细粒度权限按模块整合，支持批量权限分配
 * 解决SystemPermission过细过于繁杂的问题
 *
 * @author ErgouTree
 */
public enum PermissionModule {
    
    /**
     * 用户管理模块 - 完整权限
     */
    USER_MANAGEMENT("用户管理", "用户管理完整权限", Arrays.asList(
            SystemPermission.USER_CREATE,
            SystemPermission.USER_READ,
            SystemPermission.USER_UPDATE,
            SystemPermission.USER_DELETE,
            SystemPermission.USER_LOCK
    )),
    
    /**
     * 用户管理模块 - 基础权限（只读+编辑自己）
     */
    USER_BASIC("用户基础权限", "查看用户信息和编辑个人资料", Arrays.asList(
            SystemPermission.USER_READ,
            SystemPermission.USER_UPDATE
    )),
    
    /**
     * 角色权限管理模块 - 完整权限
     */
    ROLE_MANAGEMENT("角色管理", "角色管理完整权限", Arrays.asList(
            SystemPermission.ROLE_CREATE,
            SystemPermission.ROLE_READ,
            SystemPermission.ROLE_UPDATE,
            SystemPermission.ROLE_DELETE,
            SystemPermission.ROLE_ASSIGN
    )),
    
    /**
     * 权限管理模块 - 完整权限
     */
    PERMISSION_MANAGEMENT("权限管理", "权限管理完整权限", Arrays.asList(
            SystemPermission.PERMISSION_CREATE,
            SystemPermission.PERMISSION_READ,
            SystemPermission.PERMISSION_UPDATE,
            SystemPermission.PERMISSION_DELETE,
            SystemPermission.PERMISSION_ASSIGN
    )),
    
    /**
     * 项目管理模块 - 完整权限
     */
    PROJECT_MANAGEMENT("项目管理", "项目管理完整权限", Arrays.asList(
            SystemPermission.PROJECT_CREATE,
            SystemPermission.PROJECT_READ,
            SystemPermission.PROJECT_UPDATE,
            SystemPermission.PROJECT_DELETE,
            SystemPermission.PROJECT_ARCHIVE,
            SystemPermission.PROJECT_MEMBER_MANAGE
    )),
    
    /**
     * 项目管理模块 - 基础权限（查看+编辑，不含删除和成员管理）
     */
    PROJECT_BASIC("项目基础权限", "项目查看和编辑权限", Arrays.asList(
            SystemPermission.PROJECT_READ,
            SystemPermission.PROJECT_UPDATE
    )),
    
    /**
     * 任务管理模块 - 完整权限
     */
    TASK_MANAGEMENT("任务管理", "任务管理完整权限", Arrays.asList(
            SystemPermission.TASK_CREATE,
            SystemPermission.TASK_READ,
            SystemPermission.TASK_UPDATE,
            SystemPermission.TASK_DELETE,
            SystemPermission.TASK_ASSIGN
    )),
    
    /**
     * 任务管理模块 - 基础权限（查看+编辑自己的任务）
     */
    TASK_BASIC("任务基础权限", "任务查看和编辑权限", Arrays.asList(
            SystemPermission.TASK_READ,
            SystemPermission.TASK_UPDATE
    )),
    
    /**
     * 成果管理模块 - 完整权限
     */
    ARTIFACT_MANAGEMENT("成果管理", "成果管理完整权限", Arrays.asList(
            SystemPermission.ARTIFACT_CREATE,
            SystemPermission.ARTIFACT_READ,
            SystemPermission.ARTIFACT_UPDATE,
            SystemPermission.ARTIFACT_DELETE,
            SystemPermission.ARTIFACT_PUBLISH,
            SystemPermission.ARTIFACT_REVIEW
    )),
    
    /**
     * 成果管理模块 - 基础权限（查看+创建+编辑，不含删除和发布）
     */
    ARTIFACT_BASIC("成果基础权限", "成果查看、创建和编辑权限", Arrays.asList(
            SystemPermission.ARTIFACT_CREATE,
            SystemPermission.ARTIFACT_READ,
            SystemPermission.ARTIFACT_UPDATE
    )),
    
    /**
     * 成果审核权限（专门用于评审人员）
     */
    ARTIFACT_REVIEWER("成果评审权限", "成果评审和发布权限", Arrays.asList(
            SystemPermission.ARTIFACT_READ,
            SystemPermission.ARTIFACT_REVIEW,
            SystemPermission.ARTIFACT_PUBLISH
    )),
    
    /**
     * Wiki文档模块 - 完整权限
     */
    WIKI_MANAGEMENT("Wiki管理", "Wiki文档管理完整权限", Arrays.asList(
            SystemPermission.WIKI_CREATE,
            SystemPermission.WIKI_READ,
            SystemPermission.WIKI_UPDATE,
            SystemPermission.WIKI_DELETE
    )),
    
    /**
     * Wiki文档模块 - 基础权限（查看+编辑，不含删除）
     */
    WIKI_BASIC("Wiki基础权限", "Wiki文档查看和编辑权限", Arrays.asList(
            SystemPermission.WIKI_READ,
            SystemPermission.WIKI_UPDATE
    )),
    
    /**
     * 系统管理模块 - 完整权限（超级管理员）
     */
    SYSTEM_MANAGEMENT("系统管理", "系统管理完整权限", Arrays.asList(
            SystemPermission.SYSTEM_CONFIG,
            SystemPermission.SYSTEM_LOG,
            SystemPermission.SYSTEM_MONITOR
    )),
    
    /**
     * 系统监控权限（只读监控）
     */
    SYSTEM_MONITOR_ONLY("系统监控", "系统监控只读权限", Arrays.asList(
            SystemPermission.SYSTEM_MONITOR
    ));

    private final String moduleName;
    private final String description;
    private final List<SystemPermission> permissions;

    PermissionModule(String moduleName, String description, List<SystemPermission> permissions) {
        this.moduleName = moduleName;
        this.description = description;
        this.permissions = permissions;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getDescription() {
        return description;
    }

    public List<SystemPermission> getPermissions() {
        return permissions;
    }

    /**
     * 获取权限字符串列表
     */
    public List<String> getPermissionStrings() {
        return permissions.stream()
                .map(SystemPermission::getPermission)
                .collect(Collectors.toList());
    }

    /**
     * 获取权限数量
     */
    public int getPermissionCount() {
        return permissions.size();
    }

    /**
     * 检查是否包含指定权限
     */
    public boolean containsPermission(SystemPermission permission) {
        return permissions.contains(permission);
    }

    /**
     * 检查是否包含指定权限字符串
     */
    public boolean containsPermission(String permissionString) {
        return permissions.stream()
                .anyMatch(p -> p.getPermission().equals(permissionString));
    }
}

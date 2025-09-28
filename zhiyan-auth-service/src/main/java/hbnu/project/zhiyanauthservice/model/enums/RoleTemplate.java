package hbnu.project.zhiyanauthservice.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色模板枚举
 * 定义系统中常用的角色模板及其权限组合
 * 支持一键创建角色并分配相应权限模块
 *
 * @author ErguTree
 */
public enum RoleTemplate {
    
    /**
     * 超级管理员 - 拥有所有权限
     */
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员", "拥有系统所有权限的最高管理员", Arrays.asList(
            PermissionModule.USER_MANAGEMENT,
            PermissionModule.ROLE_MANAGEMENT,
            PermissionModule.PERMISSION_MANAGEMENT,
            PermissionModule.PROJECT_MANAGEMENT,
            PermissionModule.TASK_MANAGEMENT,
            PermissionModule.ARTIFACT_MANAGEMENT,
            PermissionModule.WIKI_MANAGEMENT,
            PermissionModule.SYSTEM_MANAGEMENT
    )),
    
    /**
     * 系统管理员 - 用户和权限管理
     */
    SYSTEM_ADMIN("SYSTEM_ADMIN", "系统管理员", "负责用户管理、角色权限管理的管理员", Arrays.asList(
            PermissionModule.USER_MANAGEMENT,
            PermissionModule.ROLE_MANAGEMENT,
            PermissionModule.PERMISSION_MANAGEMENT,
            PermissionModule.SYSTEM_MONITOR_ONLY
    )),
    
    /**
     * 项目管理员 - 项目全权管理
     */
    PROJECT_MANAGER("PROJECT_MANAGER", "项目管理员", "负责项目管理、任务分配、成果管理的管理员", Arrays.asList(
            PermissionModule.PROJECT_MANAGEMENT,
            PermissionModule.TASK_MANAGEMENT,
            PermissionModule.ARTIFACT_MANAGEMENT,
            PermissionModule.WIKI_MANAGEMENT,
            PermissionModule.USER_BASIC
    )),
    
    /**
     * 项目负责人 - 项目管理和成员协调
     */
    PROJECT_LEADER("PROJECT_LEADER", "项目负责人", "项目负责人，管理项目进度和团队协作", Arrays.asList(
            PermissionModule.PROJECT_MANAGEMENT,
            PermissionModule.TASK_MANAGEMENT,
            PermissionModule.ARTIFACT_BASIC,
            PermissionModule.WIKI_MANAGEMENT,
            PermissionModule.USER_BASIC
    )),
    
    /**
     * 研究员 - 研究和成果管理
     */
    RESEARCHER("RESEARCHER", "研究员", "参与研究项目，管理研究成果和文档", Arrays.asList(
            PermissionModule.PROJECT_BASIC,
            PermissionModule.TASK_BASIC,
            PermissionModule.ARTIFACT_BASIC,
            PermissionModule.WIKI_BASIC,
            PermissionModule.USER_BASIC
    )),
    
    /**
     * 评审专家 - 成果评审
     */
    REVIEWER("REVIEWER", "评审专家", "负责成果评审和质量把控的专家", Arrays.asList(
            PermissionModule.PROJECT_BASIC,
            PermissionModule.ARTIFACT_REVIEWER,
            PermissionModule.WIKI_BASIC,
            PermissionModule.USER_BASIC
    )),
    
    /**
     * 普通用户 - 基础权限
     */
    USER("USER", "普通用户", "系统普通用户，拥有基础查看和编辑权限", Arrays.asList(
            PermissionModule.PROJECT_BASIC,
            PermissionModule.TASK_BASIC,
            PermissionModule.ARTIFACT_BASIC,
            PermissionModule.WIKI_BASIC,
            PermissionModule.USER_BASIC
    )),
    
    /**
     * 访客 - 只读权限
     */
    GUEST("GUEST", "访客", "访客用户，只能查看公开内容", Arrays.asList(
            PermissionModule.USER_BASIC
    )),
    
    /**
     * 运维人员 - 系统监控
     */
    OPERATOR("OPERATOR", "运维人员", "负责系统运维和监控的技术人员", Arrays.asList(
            PermissionModule.SYSTEM_MANAGEMENT,
            PermissionModule.USER_BASIC
    ));

    private final String roleCode;
    private final String roleName;
    private final String description;
    private final List<PermissionModule> permissionModules;

    RoleTemplate(String roleCode, String roleName, String description, List<PermissionModule> permissionModules) {
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.description = description;
        this.permissionModules = permissionModules;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public List<PermissionModule> getPermissionModules() {
        return permissionModules;
    }

    /**
     * 获取所有权限（扁平化）
     */
    public List<SystemPermission> getAllPermissions() {
        return permissionModules.stream()
                .flatMap(module -> module.getPermissions().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取所有权限字符串（扁平化）
     */
    public List<String> getAllPermissionStrings() {
        return getAllPermissions().stream()
                .map(SystemPermission::getPermission)
                .collect(Collectors.toList());
    }

    /**
     * 获取权限模块数量
     */
    public int getModuleCount() {
        return permissionModules.size();
    }

    /**
     * 获取总权限数量
     */
    public int getTotalPermissionCount() {
        return getAllPermissions().size();
    }

    /**
     * 检查是否包含指定权限模块
     */
    public boolean containsModule(PermissionModule module) {
        return permissionModules.contains(module);
    }

    /**
     * 检查是否包含指定权限
     */
    public boolean containsPermission(SystemPermission permission) {
        return getAllPermissions().contains(permission);
    }

    /**
     * 检查是否包含指定权限字符串
     */
    public boolean containsPermission(String permissionString) {
        return getAllPermissionStrings().contains(permissionString);
    }

    /**
     * 根据角色代码查找角色模板
     */
    public static RoleTemplate findByCode(String roleCode) {
        for (RoleTemplate template : values()) {
            if (template.getRoleCode().equals(roleCode)) {
                return template;
            }
        }
        return null;
    }

    /**
     * 根据角色名称查找角色模板
     */
    public static RoleTemplate findByName(String roleName) {
        for (RoleTemplate template : values()) {
            if (template.getRoleName().equals(roleName)) {
                return template;
            }
        }
        return null;
    }
}

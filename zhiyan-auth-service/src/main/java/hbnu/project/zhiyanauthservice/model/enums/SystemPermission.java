package hbnu.project.zhiyanauthservice.model.enums;

/**
 * 系统权限枚举
 * 定义系统中的所有权限标识符
 * 过细过于繁杂，需要重新设计
 *
 * @author ErgouTree
 */
public enum SystemPermission {
    
    // ============ 用户管理权限 ============
    USER_CREATE("user:create", "创建用户"),
    USER_READ("user:read", "查看用户信息"),
    USER_UPDATE("user:update", "更新用户信息"),
    USER_DELETE("user:delete", "删除用户"),
    USER_LOCK("user:lock", "锁定/解锁用户"),
    
    // ============ 角色权限管理 ============
    ROLE_CREATE("role:create", "创建角色"),
    ROLE_READ("role:read", "查看角色"),
    ROLE_UPDATE("role:update", "更新角色"),
    ROLE_DELETE("role:delete", "删除角色"),
    ROLE_ASSIGN("role:assign", "分配角色给用户"),
    
    // ============ 权限管理 ============
    PERMISSION_CREATE("permission:create", "创建权限"),
    PERMISSION_READ("permission:read", "查看权限"),
    PERMISSION_UPDATE("permission:update", "更新权限"),
    PERMISSION_DELETE("permission:delete", "删除权限"),
    PERMISSION_ASSIGN("permission:assign", "分配权限给角色"),
    
    // ============ 项目管理权限 ============
    PROJECT_CREATE("project:create", "创建项目"),
    PROJECT_READ("project:read", "查看项目"),
    PROJECT_UPDATE("project:update", "更新项目"),
    PROJECT_DELETE("project:delete", "删除项目"),
    PROJECT_ARCHIVE("project:archive", "归档项目"),
    PROJECT_MEMBER_MANAGE("project:member:manage", "管理项目成员"),
    
    // ============ 任务管理权限 ============
    TASK_CREATE("task:create", "创建任务"),
    TASK_READ("task:read", "查看任务"),
    TASK_UPDATE("task:update", "更新任务"),
    TASK_DELETE("task:delete", "删除任务"),
    TASK_ASSIGN("task:assign", "分配任务"),
    
    // ============ 成果管理权限 ============
    ARTIFACT_CREATE("artifact:create", "创建成果"),
    ARTIFACT_READ("artifact:read", "查看成果"),
    ARTIFACT_UPDATE("artifact:update", "更新成果"),
    ARTIFACT_DELETE("artifact:delete", "删除成果"),
    ARTIFACT_PUBLISH("artifact:publish", "发布成果"),
    ARTIFACT_REVIEW("artifact:review", "评审成果"),
    
    // ============ Wiki文档权限 ============
    WIKI_CREATE("wiki:create", "创建Wiki文档"),
    WIKI_READ("wiki:read", "查看Wiki文档"),
    WIKI_UPDATE("wiki:update", "编辑Wiki文档"),
    WIKI_DELETE("wiki:delete", "删除Wiki文档"),
    
    // ============ 系统管理权限 ============
    SYSTEM_CONFIG("system:config", "系统配置管理"),
    SYSTEM_LOG("system:log", "查看系统日志"),
    SYSTEM_MONITOR("system:monitor", "系统监控");

    private final String permission;
    private final String description;

    SystemPermission(String permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }
}

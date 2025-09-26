package hbnu.project.zhiyanauthservice.model.enums;

/**
 * 系统角色枚举
 * 定义系统级别的预定义角色
 *
 * @author ErgouTree
 */
public enum SystemRole {
    
    /**
     * 开发者
     */
    DEVELOPER("开发者", "拥有系统所有权限"),
    
    /**
     * 系统管理员
     */
    ADMIN("系统管理员", "拥有系统管理权限"),
    
    /**
     * 导师
     */
    TEACHER("导师", "可以创建和管理项目"),

    /**
     * 团队负责人
     */
    TEAM_LEADER("导师", "可以创建和管理项目"),

    /**
     * 普通用户
     */
    USER("普通用户", "基础用户权限"),
    
    /**
     * 访客用户
     */
    GUEST("访客用户", "只读权限");

    private final String roleName;
    private final String description;

    SystemRole(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }
}

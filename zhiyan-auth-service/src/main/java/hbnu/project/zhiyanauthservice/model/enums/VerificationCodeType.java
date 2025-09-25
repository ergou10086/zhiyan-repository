package hbnu.project.zhiyanauthservice.model.enums;

/**
 * 验证码类型枚举
 *
 * @author ErgouTree
 */
public enum VerificationCodeType {
    /**
     * 用户注册验证码
     */
    REGISTER("用户注册"),

    /**
     * 重置密码验证码
     */
    RESET_PASSWORD("重置密码"),

    /**
     * 修改邮箱验证码
     */
    CHANGE_EMAIL("修改邮箱"),

    /**
     * 登录二次验证码（可选功能）
     */
    LOGIN_VERIFICATION("登录验证");

    private final String description;

    VerificationCodeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
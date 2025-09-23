package hbnu.project.zhiyanauthservice.model.form;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户登录对象
 *
 * @author ErgouTree
 */
@Getter
@Setter
public class LoginBody
{
    /**
     * 登录邮箱（替代原用户名，与产品设计保持一致）
     */
    private String email;

    /**
     * 用户密码
     */
    private String password;

    /**
     * "记住我"选项（新增，支持长效会话）
     */
    private boolean rememberMe;
}

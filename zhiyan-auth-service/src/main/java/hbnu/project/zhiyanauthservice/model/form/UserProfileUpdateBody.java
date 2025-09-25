package hbnu.project.zhiyanauthservice.model.form;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户资料更新表单
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateBody {

    /**
     * 用户姓名
     */
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String name;

    /**
     * 用户职称/职位
     */
    @Size(max = 100, message = "职称/职位长度不能超过100个字符")
    private String title;

    /**
     * 所属机构
     */
    @Size(max = 200, message = "所属机构长度不能超过200个字符")
    private String institution;

    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;
}

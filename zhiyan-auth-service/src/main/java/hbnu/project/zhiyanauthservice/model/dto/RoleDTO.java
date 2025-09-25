package hbnu.project.zhiyanauthservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色数据传输对象
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;
}
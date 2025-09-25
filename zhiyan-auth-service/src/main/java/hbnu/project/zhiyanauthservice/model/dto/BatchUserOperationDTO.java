package hbnu.project.zhiyanauthservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量用户操作DTO
 * 用于批量处理用户相关操作
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUserOperationDTO {

    /**
     * 用户ID列表
     */
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 操作类型
     */
    @NotNull(message = "操作类型不能为空")
    private OperationType operation;

    /**
     * 操作参数（可选）
     * 如：角色ID、权限列表等
     */
    private Object operationParams;

    /**
     * 操作原因（可选）
     */
    private String reason;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        LOCK,           // 锁定用户
        UNLOCK,         // 解锁用户
        DELETE,         // 删除用户
        ASSIGN_ROLE,    // 分配角色
        REMOVE_ROLE,    // 移除角色
        RESET_PASSWORD  // 重置密码
    }
}

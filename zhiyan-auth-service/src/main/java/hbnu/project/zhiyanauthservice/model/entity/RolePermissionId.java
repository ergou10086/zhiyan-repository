package hbnu.project.zhiyanauthservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 角色权限关联复合主键
 *
 * @author ErgouTree
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionId implements java.io.Serializable {

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "permission_id")
    private Long permissionId;
}

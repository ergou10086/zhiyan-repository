package hbnu.project.zhiyanauthservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * 权限实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '权限唯一标识'")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '权限名称（如：project:create、task:edit）'")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT COMMENT '权限描述（如：创建项目权限、编辑任务权限）'")
    private String description;

    // 角色权限关联（一对多）
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RolePermission> rolePermissions;
}

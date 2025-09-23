package hbnu.project.zhiyanauthservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * 角色实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '角色唯一标识'")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50,
            columnDefinition = "VARCHAR(50) COMMENT '角色名称（如：ADMIN、USER）'")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT COMMENT '角色描述（如：系统管理员、普通用户）'")
    private String description;

    // 角色用户关联（一对多）
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRole> userRoles;

    // 角色权限关联（一对多）
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RolePermission> rolePermissions;
}

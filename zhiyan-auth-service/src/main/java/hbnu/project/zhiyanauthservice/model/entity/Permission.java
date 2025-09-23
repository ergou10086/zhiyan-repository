package hbnu.project.zhiyanauthservice.model.entity;

import hbnu.project.zhiyancommon.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
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
@EntityListeners(AuditingEntityListener.class) // 启用 JPA 审计
public class Permission {

    /**
     * 雪花id
     */
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '权限唯一标识'")
    private Long id;

    /**
     * 权限名称
     */
    @Column(name = "name", nullable = false, unique = true, length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '权限名称（如：project:create、task:edit）'")
    private String name;

    /**
     * 权限描述
     */
    @Column(name = "description", columnDefinition = "TEXT COMMENT '权限描述（如：创建项目权限、编辑任务权限）'")
    private String description;

    /**
     * 数据创建时间（由审计自动填充）
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 数据最后修改时间（由审计自动更新）
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 数据创建人（由审计自动填充）
     */
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    /**
     * 数据最后修改人（由审计自动更新）
     */
    @LastModifiedBy
    private String updatedBy;


    /**
     * 角色权限关联（一对多）
     */
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RolePermission> rolePermissions;


    /**
     * 在持久化之前生成雪花ID
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
    }
}

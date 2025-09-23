package hbnu.project.zhiyanauthservice.model.entity;

import hbnu.project.zhiyancommon.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_ROLE_PERMISSION",
                columnNames = {"role_id", "permission_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '关联记录唯一标识（雪花ID）'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_ROLE_PERMISSION_ROLE"))
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_ROLE_PERMISSION_PERMISSION"))
    private Permission permission;

    @CreationTimestamp
    @Column(name = "granted_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '权限授予时间'")
    private LocalDateTime grantedAt;

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
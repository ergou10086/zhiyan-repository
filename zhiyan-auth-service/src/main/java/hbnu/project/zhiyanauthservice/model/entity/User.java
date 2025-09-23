package hbnu.project.zhiyanauthservice.model.entity;

import hbnu.project.zhiyancommon.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 用户实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '用户唯一标识（雪花ID）'")
    private Long id;

    @Column(name = "email", nullable = false, unique = true,
            columnDefinition = "VARCHAR(255) COMMENT '用户邮箱（登录账号）'")
    private String email;

    @Column(name = "password_hash", nullable = false,
            columnDefinition = "VARCHAR(255) COMMENT '密码哈希值（加密存储）'")
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '用户姓名'")
    private String name;

    @Column(name = "avatar_url", length = 500,
            columnDefinition = "VARCHAR(500) COMMENT '用户头像URL'")
    private String avatarUrl;

    @Column(name = "title", length = 100,
            columnDefinition = "VARCHAR(100) COMMENT '用户职称/职位'")
    private String title;

    @Column(name = "institution", length = 200,
            columnDefinition = "VARCHAR(200) COMMENT '用户所属机构'")
    private String institution;

    @Column(name = "is_locked", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '是否锁定（禁止登录）'")
    private Boolean isLocked = false;

    @Column(name = "is_deleted", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE COMMENT '软删除标记'")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'")
    private LocalDateTime updatedAt;

    // 用户角色关联（一对多）
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRole> userRoles;

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
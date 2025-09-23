package hbnu.project.zhiyanprojectservice.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import hbnu.project.zhiyanprojectservice.model.enums.ProjectRole;
import hbnu.project.zhiyanprojectservice.model.enums.ProjectStatus;
import hbnu.project.zhiyanprojectservice.model.enums.ProjectVisibility;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目成员实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "project_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}),
        indexes = {
                @Index(name = "idx_project", columnList = "project_id"),
                @Index(name = "idx_user", columnList = "user_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '成员记录唯一标识'")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_PROJECT_MEMBER_PROJECT"))
    private Project project;


    @Column(name = "user_id", nullable = false,
            columnDefinition = "BIGINT COMMENT '用户ID（逻辑关联users表）'")
    private Long userId;


    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false,
            columnDefinition = "ENUM('LEADER', 'MAINTAINER', 'MEMBER') COMMENT '项目内角色'")
    private ProjectRole projectRole;


    @Column(name = "permissions_override", columnDefinition = "JSON COMMENT '权限覆盖'")
    private String permissionsOverride;


    @CreationTimestamp
    @Column(name = "joined_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入项目时间'")
    private LocalDateTime joinedAt;
}

package hbnu.project.zhiyanprojectservice.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import hbnu.project.zhiyanprojectservice.model.enums.ProjectStatus;
import hbnu.project.zhiyanprojectservice.model.enums.ProjectVisibility;
import hbnu.project.zhiyanprojectservice.model.enums.RequestStatus;
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
 * 项目加入申请实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "project_join_requests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id", "status"}),
        indexes = {
                @Index(name = "idx_project_status", columnList = "project_id, status"),
                @Index(name = "idx_user", columnList = "user_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '申请记录唯一标识'")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_JOIN_REQUEST_PROJECT"))
    private Project project;

    @Column(name = "user_id", nullable = false,
            columnDefinition = "BIGINT COMMENT '申请人ID（逻辑关联users表）'")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false,
            columnDefinition = "ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '申请状态'")
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "message", columnDefinition = "TEXT COMMENT '申请说明'")
    private String message;

    @Column(name = "responded_at", columnDefinition = "TIMESTAMP COMMENT '处理时间'")
    private LocalDateTime respondedAt;

    @Column(name = "responded_by", columnDefinition = "BIGINT COMMENT '处理人ID（逻辑关联users表）'")
    private Long respondedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间'")
    private LocalDateTime createdAt;
}

package hbnu.project.zhiyanprojectservice.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import hbnu.project.zhiyanprojectservice.model.enums.TaskPriority;
import hbnu.project.zhiyanprojectservice.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "tasks",
        indexes = {
                @Index(name = "idx_assignee_id", columnList = "assignee_id"),
                @Index(name = "idx_created_by", columnList = "created_by")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '任务唯一标识'")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_TASK_PROJECT"))
    private Project project;


    @Column(name = "title", nullable = false, length = 200,
            columnDefinition = "VARCHAR(200) COMMENT '任务标题'")
    private String title;


    @Column(name = "description", columnDefinition = "TEXT COMMENT '任务描述'")
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false,
            columnDefinition = "ENUM('TODO', 'IN_PROGRESS', 'BLOCKED', 'DONE') DEFAULT 'TODO' COMMENT '任务状态'")
    private TaskStatus status = TaskStatus.TODO;


    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false,
            columnDefinition = "ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM' COMMENT '任务优先级'")
    private TaskPriority priority = TaskPriority.MEDIUM;


    @Column(name = "assignee_id", nullable = false, columnDefinition = "JSON COMMENT '负责人ID（JSON数组）'")
    private String assigneeId;


    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "due_date", columnDefinition = "DATE COMMENT '任务截止日期'")
    private LocalDate dueDate;


    @Column(name = "created_by", nullable = false,
            columnDefinition = "BIGINT COMMENT '创建人ID（逻辑关联用户服务的用户ID）'")
    private Long createdBy;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'")
    private LocalDateTime updatedAt;
}

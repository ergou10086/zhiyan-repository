package hbnu.project.zhiyanprojectservice.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 项目实体类
 */
@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '项目唯一标识'")
    private Long id;


    @Column(name = "name", nullable = false, length = 200,
            columnDefinition = "VARCHAR(200) COMMENT '项目名称'")
    private String name;


    @Column(name = "description", columnDefinition = "TEXT COMMENT '项目描述'")
    private String description;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false,
            columnDefinition = "ENUM('PLANNING', 'ONGOING', 'COMPLETED', 'ARCHIVED') DEFAULT 'PLANNING' COMMENT '项目状态'")
    private ProjectStatus status = ProjectStatus.PLANNING;


    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false,
            columnDefinition = "ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PRIVATE' COMMENT '项目可见性'")
    private ProjectVisibility visibility = ProjectVisibility.PRIVATE;


    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "start_date", columnDefinition = "DATE COMMENT '项目开始日期'")
    private LocalDate startDate;


    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "end_date", columnDefinition = "DATE COMMENT '项目结束日期'")
    private LocalDate endDate;


    @Column(name = "created_by", nullable = false,
            columnDefinition = "BIGINT COMMENT '创建人ID（逻辑关联users表）'")
    private Long createdBy;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'")
    private LocalDateTime updatedAt;


    // 项目成员关联（一对多）
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectMember> projectMembers;


    // 项目任务关联（一对多）
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks;


    // 项目加入申请关联（一对多）
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectJoinRequest> joinRequests;
}

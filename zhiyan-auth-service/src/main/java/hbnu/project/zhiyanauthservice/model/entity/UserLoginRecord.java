package hbnu.project.zhiyanauthservice.model.entity;

import hbnu.project.zhiyancommon.utils.id.SnowflakeIdUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 用户登录记录实体类
 * 用于记录用户登录历史，便于安全审计和行为分析
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "user_login_records", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_login_time", columnList = "login_time"),
        @Index(name = "idx_client_ip", columnList = "client_ip")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRecord{

    /**
     * 雪花ID
     */
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT COMMENT '登录记录唯一标识（雪花ID）'")
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false,
            columnDefinition = "BIGINT COMMENT '用户ID'")
    private Long userId;

    /**
     * 登录时间
     */
    @CreatedDate
    @Column(name = "login_time", nullable = false, updatable = false,
            columnDefinition = "DATETIME COMMENT '登录时间'")
    private LocalDateTime loginTime;

    /**
     * 客户端IP地址
     */
    @Column(name = "client_ip", nullable = false, length = 45,
            columnDefinition = "VARCHAR(45) COMMENT '客户端IP地址'")
    private String clientIp;

    /**
     * 用户代理信息
     */
    @Column(name = "user_agent", length = 500,
            columnDefinition = "VARCHAR(500) COMMENT '用户代理信息（浏览器、设备等）'")
    private String userAgent;

    /**
     * 登录结果
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "login_result", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) COMMENT '登录结果（SUCCESS/FAILED）'")
    private LoginResult loginResult;

    /**
     * 失败原因
     */
    @Column(name = "failure_reason", length = 200,
            columnDefinition = "VARCHAR(200) COMMENT '登录失败原因'")
    private String failureReason;

    /**
     * 登录方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) COMMENT '登录方式（EMAIL_PASSWORD/THIRD_PARTY）'")
    private LoginType loginType = LoginType.EMAIL_PASSWORD;

    /**
     * 地理位置信息（可选）
     */
    @Column(name = "location", length = 200,
            columnDefinition = "VARCHAR(200) COMMENT '登录地理位置'")
    private String location;

    /**
     * 在持久化之前生成雪花ID
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
    }

    /**
     * 登录结果枚举
     */
    public enum LoginResult {
        SUCCESS, FAILED
    }

    /**
     * 登录方式枚举
     */
    public enum LoginType {
        EMAIL_PASSWORD, THIRD_PARTY
    }
}

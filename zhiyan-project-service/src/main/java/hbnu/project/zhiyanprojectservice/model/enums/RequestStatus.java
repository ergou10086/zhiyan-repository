package hbnu.project.zhiyanprojectservice.model.enums;

import lombok.Getter;

/**
 * 申请状态枚举
 *
 * @author ErgouTree
 */
@Getter
public enum RequestStatus {
    PENDING("待处理"),
    APPROVED("已批准"),
    REJECTED("已拒绝");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

}

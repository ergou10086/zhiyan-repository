package hbnu.project.zhiyanprojectservice.model.enums;

import lombok.Getter;

/**
 * 项目状态枚举
 * @author ErgouTree
 */
@Getter
public enum ProjectStatus {
    PLANNING("规划中"),
    ONGOING("进行中"),
    COMPLETED("已完成"),
    ARCHIVED("已归档");

    private final String description;

    ProjectStatus(String description) {
        this.description = description;
    }

}

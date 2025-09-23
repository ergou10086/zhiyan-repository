package hbnu.project.zhiyanprojectservice.model.enums;

import lombok.Getter;

/**
 * 任务优先级枚举
 *
 * @author ErgouTree
 */
@Getter
public enum TaskPriority {
    HIGH("高"),
    MEDIUM("中"),
    LOW("低");

    private final String description;

    TaskPriority(String description) {
        this.description = description;
    }

}

package hbnu.project.zhiyanprojectservice.model.enums;


import lombok.Getter;

/**
 * 任务状态枚举
 *
 * @author ErgouTree
 */
@Getter
public enum TaskStatus {
    TODO("待办"),
    IN_PROGRESS("进行中"),
    BLOCKED("阻塞"),
    DONE("已完成");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

}

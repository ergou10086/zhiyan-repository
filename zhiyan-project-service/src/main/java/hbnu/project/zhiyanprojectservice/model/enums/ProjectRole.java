package hbnu.project.zhiyanprojectservice.model.enums;

import lombok.Getter;

/**
 * 项目角色枚举
 *
 * @author ErgouTree
 */
@Getter
public enum ProjectRole {
    LEADER("负责人"),
    MAINTAINER("维护者"),
    MEMBER("普通成员");

    private final String description;

    ProjectRole(String description) {
        this.description = description;
    }

}

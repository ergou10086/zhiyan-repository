package hbnu.project.zhiyanprojectservice.model.enums;

import lombok.Getter;

/**
 * 项目可见性枚举
 * @author ErgouTree
 */
@Getter
public enum ProjectVisibility {
    PUBLIC("公开"),
    PRIVATE("私有");

    private final String description;

    ProjectVisibility(String description) {
        this.description = description;
    }

}

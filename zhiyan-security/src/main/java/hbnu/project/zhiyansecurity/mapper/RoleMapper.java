package hbnu.project.zhiyansecurity.mapper;

import hbnu.project.zhiyanauthservice.model.dto.RoleDTO;
import hbnu.project.zhiyanauthservice.model.entity.Role;
import hbnu.project.zhiyanauthservice.model.entity.RolePermission;
import hbnu.project.zhiyancommon.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色实体转换器
 * 提供Role实体与RoleDTO之间的可靠转换
 *
 * @author ErgouTree
 */
@Component
public class RoleMapper {

    /**
     * Role实体转换为RoleDTO
     *
     * @param role Role实体对象
     * @return RoleDTO对象，如果输入为null则返回null
     */
    public RoleDTO toRoleDTO(Role role) {
        if (role == null) {
            return null;
        }

        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(extractPermissionNames(role.getRolePermissions()))
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .build();
    }

    /**
     * RoleDTO转换为Role实体（用于创建新角色）
     *
     * @param roleDTO RoleDTO对象
     * @return Role实体对象，如果输入为null则返回null
     */
    public Role toRole(RoleDTO roleDTO) {
        if (roleDTO == null) {
            return null;
        }

        return Role.builder()
                .id(roleDTO.getId())
                .name(roleDTO.getName())
                .description(roleDTO.getDescription())
                .build();
    }

    /**
     * Role实体列表转换为RoleDTO列表
     *
     * @param roles Role实体列表
     * @return RoleDTO列表，如果输入为null则返回null
     */
    public List<RoleDTO> toRoleDTOList(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(this::toRoleDTO)
                .collect(Collectors.toList());
    }

    /**
     * RoleDTO列表转换为Role实体列表
     *
     * @param roleDTOs RoleDTO列表
     * @return Role实体列表，如果输入为null则返回null
     */
    public List<Role> toRoleList(List<RoleDTO> roleDTOs) {
        if (roleDTOs == null) {
            return null;
        }
        return roleDTOs.stream()
                .map(this::toRole)
                .collect(Collectors.toList());
    }

    /**
     * 更新Role实体的基本信息（不包括关联关系）
     *
     * @param role 待更新的Role实体
     * @param roleDTO 包含新信息的RoleDTO
     * @return 更新后的Role实体
     */
    public Role updateRoleFromDTO(Role role, RoleDTO roleDTO) {
        if (role == null || roleDTO == null) {
            return role;
        }

        if (StringUtils.isNotBlank(roleDTO.getName())) {
            role.setName(roleDTO.getName());
        }
        if (StringUtils.isNotBlank(roleDTO.getDescription())) {
            role.setDescription(roleDTO.getDescription());
        }

        return role;
    }

    /**
     * 从角色权限关联中提取权限名称列表
     *
     * @param rolePermissions 角色权限关联列表
     * @return 权限名称列表
     */
    private List<String> extractPermissionNames(List<RolePermission> rolePermissions) {
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return List.of();
        }

        return rolePermissions.stream()
                .filter(rolePermission -> rolePermission.getPermission() != null)
                .map(rolePermission -> rolePermission.getPermission().getName())
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 创建简化的RoleDTO（仅包含基本信息）
     *
     * @param id 角色ID
     * @param name 角色名称
     * @param description 角色描述
     * @return 简化的RoleDTO对象
     */
    public RoleDTO createSimpleRoleDTO(Long id, String name, String description) {
        if (id == null || StringUtils.isBlank(name)) {
            return null;
        }

        return RoleDTO.builder()
                .id(id)
                .name(name)
                .description(description)
                .permissions(List.of())
                .build();
    }

    /**
     * 验证RoleDTO的必要字段
     *
     * @param roleDTO 待验证的RoleDTO
     * @return 验证结果，true表示通过
     */
    public boolean validateRoleDTO(RoleDTO roleDTO) {
        if (roleDTO == null) {
            return false;
        }

        // 角色名称不能为空
        if (StringUtils.isBlank(roleDTO.getName())) {
            return false;
        }

        // 角色名称长度检查
        if (roleDTO.getName().length() > 50) {
            return false;
        }

        return true;
    }

    /**
     * 创建系统默认角色的RoleDTO
     *
     * @param roleName 角色名称
     * @return 系统角色的RoleDTO
     */
    public RoleDTO createSystemRoleDTO(String roleName) {
        if (StringUtils.isBlank(roleName)) {
            return null;
        }

        String description = switch (roleName.toUpperCase()) {
            case "ADMIN" -> "系统管理员";
            case "USER" -> "普通用户";
            case "GUEST" -> "访客用户";
            default -> "自定义角色";
        };

        return RoleDTO.builder()
                .name(roleName)
                .description(description)
                .permissions(List.of())
                .build();
    }
}

package hbnu.project.zhiyansecurity.mapper;

import hbnu.project.zhiyanauthservice.model.dto.PermissionDTO;
import hbnu.project.zhiyanauthservice.model.entity.Permission;
import hbnu.project.zhiyancommon.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限实体转换器
 * 提供Permission实体与PermissionDTO之间的可靠转换
 *
 * @author ErgouTree
 */
@Component
public class PermissionMapper {

    /**
     * Permission实体转换为PermissionDTO
     *
     * @param permission Permission实体对象
     * @return PermissionDTO对象，如果输入为null则返回null
     */
    public PermissionDTO toPermissionDTO(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionDTO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    /**
     * PermissionDTO转换为Permission实体（用于创建新权限）
     *
     * @param permissionDTO PermissionDTO对象
     * @return Permission实体对象，如果输入为null则返回null
     */
    public Permission toPermission(PermissionDTO permissionDTO) {
        if (permissionDTO == null) {
            return null;
        }

        return Permission.builder()
                .id(permissionDTO.getId())
                .name(permissionDTO.getName())
                .description(permissionDTO.getDescription())
                .build();
    }

    /**
     * Permission实体列表转换为PermissionDTO列表
     *
     * @param permissions Permission实体列表
     * @return PermissionDTO列表，如果输入为null则返回null
     */
    public List<PermissionDTO> toPermissionDTOList(List<Permission> permissions) {
        if (permissions == null) {
            return null;
        }
        return permissions.stream()
                .map(this::toPermissionDTO)
                .collect(Collectors.toList());
    }

    /**
     * PermissionDTO列表转换为Permission实体列表
     *
     * @param permissionDTOs PermissionDTO列表
     * @return Permission实体列表，如果输入为null则返回null
     */
    public List<Permission> toPermissionList(List<PermissionDTO> permissionDTOs) {
        if (permissionDTOs == null) {
            return null;
        }
        return permissionDTOs.stream()
                .map(this::toPermission)
                .collect(Collectors.toList());
    }

    /**
     * 更新Permission实体的基本信息
     *
     * @param permission 待更新的Permission实体
     * @param permissionDTO 包含新信息的PermissionDTO
     * @return 更新后的Permission实体
     */
    public Permission updatePermissionFromDTO(Permission permission, PermissionDTO permissionDTO) {
        if (permission == null || permissionDTO == null) {
            return permission;
        }

        if (StringUtils.isNotBlank(permissionDTO.getName())) {
            permission.setName(permissionDTO.getName());
        }
        if (StringUtils.isNotBlank(permissionDTO.getDescription())) {
            permission.setDescription(permissionDTO.getDescription());
        }

        return permission;
    }

    /**
     * 创建简化的PermissionDTO（仅包含基本信息）
     *
     * @param id 权限ID
     * @param name 权限名称
     * @param description 权限描述
     * @return 简化的PermissionDTO对象
     */
    public PermissionDTO createSimplePermissionDTO(Long id, String name, String description) {
        if (id == null || StringUtils.isBlank(name)) {
            return null;
        }

        return PermissionDTO.builder()
                .id(id)
                .name(name)
                .description(description)
                .build();
    }

    /**
     * 验证PermissionDTO的必要字段
     *
     * @param permissionDTO 待验证的PermissionDTO
     * @return 验证结果，true表示通过
     */
    public boolean validatePermissionDTO(PermissionDTO permissionDTO) {
        if (permissionDTO == null) {
            return false;
        }

        // 权限名称不能为空
        if (StringUtils.isBlank(permissionDTO.getName())) {
            return false;
        }

        // 权限名称长度检查
        if (permissionDTO.getName().length() > 100) {
            return false;
        }

        return true;
    }

    /**
     * 创建系统默认权限的PermissionDTO
     *
     * @param permissionName 权限名称
     * @return 系统权限的PermissionDTO
     */
    public PermissionDTO createSystemPermissionDTO(String permissionName) {
        if (StringUtils.isBlank(permissionName)) {
            return null;
        }

        String description = switch (permissionName.toUpperCase()) {
            case "USER_READ" -> "用户信息查看权限";
            case "USER_WRITE" -> "用户信息编辑权限";
            case "USER_DELETE" -> "用户删除权限";
            case "ROLE_READ" -> "角色查看权限";
            case "ROLE_WRITE" -> "角色编辑权限";
            case "ROLE_DELETE" -> "角色删除权限";
            case "PERMISSION_READ" -> "权限查看权限";
            case "PERMISSION_WRITE" -> "权限编辑权限";
            case "PERMISSION_DELETE" -> "权限删除权限";
            case "SYSTEM_ADMIN" -> "系统管理权限";
            default -> "自定义权限";
        };

        return PermissionDTO.builder()
                .name(permissionName)
                .description(description)
                .build();
    }

    /**
     * 权限名称列表转换为PermissionDTO列表（仅包含名称）
     *
     * @param permissionNames 权限名称列表
     * @return PermissionDTO列表
     */
    public List<PermissionDTO> fromPermissionNames(List<String> permissionNames) {
        if (permissionNames == null || permissionNames.isEmpty()) {
            return List.of();
        }

        return permissionNames.stream()
                .filter(StringUtils::isNotBlank)
                .map(name -> PermissionDTO.builder()
                        .name(name)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * PermissionDTO列表转换为权限名称列表
     *
     * @param permissionDTOs PermissionDTO列表
     * @return 权限名称列表
     */
    public List<String> toPermissionNames(List<PermissionDTO> permissionDTOs) {
        if (permissionDTOs == null || permissionDTOs.isEmpty()) {
            return List.of();
        }

        return permissionDTOs.stream()
                .filter(dto -> dto != null && StringUtils.isNotBlank(dto.getName()))
                .map(PermissionDTO::getName)
                .distinct()
                .collect(Collectors.toList());
    }
}

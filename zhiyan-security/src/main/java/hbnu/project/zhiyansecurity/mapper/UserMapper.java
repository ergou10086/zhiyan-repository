package hbnu.project.zhiyansecurity.mapper;

import hbnu.project.zhiyanauthservice.model.dto.UserDTO;
import hbnu.project.zhiyanauthservice.model.entity.User;
import hbnu.project.zhiyanauthservice.model.entity.UserRole;
import hbnu.project.zhiyanauthservice.model.entity.RolePermission;
import hbnu.project.zhiyansecurity.context.LoginUserBody;
import hbnu.project.zhiyancommon.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户实体转换器
 * 提供User实体与各种DTO之间的可靠转换
 *
 * @author ErgouTree
 */
@Component
public class UserMapper {

    /**
     * User实体转换为UserDTO
     *
     * @param user User实体对象
     * @return UserDTO对象，如果输入为null则返回null
     */
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .title(user.getTitle())
                .institution(user.getInstitution())
                .isLocked(user.getIsLocked())
                .roles(extractRoleNames(user.getUserRoles()))
                .permissions(extractPermissionNames(user.getUserRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * User实体转换为LoginUserBody
     *
     * @param user User实体对象
     * @return LoginUserBody对象，如果输入为null则返回null
     */
    public LoginUserBody toLoginUserBody(User user) {
        if (user == null) {
            return null;
        }

        return LoginUserBody.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .title(user.getTitle())
                .institution(user.getInstitution())
                .isLocked(user.getIsLocked())
                .roles(extractRoleNames(user.getUserRoles()))
                .permissions(extractPermissionSet(user.getUserRoles()))
                .build();
    }

    /**
     * UserDTO转换为LoginUserBody
     *
     * @param userDTO UserDTO对象
     * @return LoginUserBody对象，如果输入为null则返回null
     */
    public LoginUserBody toLoginUserBody(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        return LoginUserBody.builder()
                .userId(userDTO.getId())
                .email(userDTO.getEmail())
                .name(userDTO.getName())
                .avatarUrl(userDTO.getAvatarUrl())
                .title(userDTO.getTitle())
                .institution(userDTO.getInstitution())
                .isLocked(userDTO.getIsLocked())
                .roles(userDTO.getRoles())
                .permissions(userDTO.getPermissions() != null ? 
                    Set.copyOf(userDTO.getPermissions()) : null)
                .build();
    }

    /**
     * LoginUserBody转换为UserDTO
     *
     * @param loginUserBody LoginUserBody对象
     * @return UserDTO对象，如果输入为null则返回null
     */
    public UserDTO toUserDTO(LoginUserBody loginUserBody) {
        if (loginUserBody == null) {
            return null;
        }

        return UserDTO.builder()
                .id(loginUserBody.getUserId())
                .email(loginUserBody.getEmail())
                .name(loginUserBody.getName())
                .avatarUrl(loginUserBody.getAvatarUrl())
                .title(loginUserBody.getTitle())
                .institution(loginUserBody.getInstitution())
                .isLocked(loginUserBody.getIsLocked())
                .roles(loginUserBody.getRoles())
                .permissions(loginUserBody.getPermissions() != null ? 
                    List.copyOf(loginUserBody.getPermissions()) : null)
                .build();
    }

    /**
     * User实体列表转换为UserDTO列表
     *
     * @param users User实体列表
     * @return UserDTO列表，如果输入为null则返回null
     */
    public List<UserDTO> toUserDTOList(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * 从用户角色关联中提取角色名称列表
     *
     * @param userRoles 用户角色关联列表
     * @return 角色名称列表
     */
    private List<String> extractRoleNames(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return List.of();
        }

        return userRoles.stream()
                .filter(userRole -> userRole.getRole() != null)
                .map(userRole -> userRole.getRole().getName())
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 从用户角色关联中提取权限名称列表
     *
     * @param userRoles 用户角色关联列表
     * @return 权限名称列表
     */
    private List<String> extractPermissionNames(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return List.of();
        }

        return userRoles.stream()
                .filter(userRole -> userRole.getRole() != null)
                .flatMap(userRole -> userRole.getRole().getRolePermissions().stream())
                .filter(rolePermission -> rolePermission.getPermission() != null)
                .map(rolePermission -> rolePermission.getPermission().getName())
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 从用户角色关联中提取权限名称集合
     *
     * @param userRoles 用户角色关联列表
     * @return 权限名称集合
     */
    private Set<String> extractPermissionSet(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return Set.of();
        }

        return userRoles.stream()
                .filter(userRole -> userRole.getRole() != null)
                .flatMap(userRole -> userRole.getRole().getRolePermissions().stream())
                .filter(rolePermission -> rolePermission.getPermission() != null)
                .map(rolePermission -> rolePermission.getPermission().getName())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    /**
     * 更新User实体的基本信息（不包括密码和关联关系）
     *
     * @param user 待更新的User实体
     * @param userDTO 包含新信息的UserDTO
     * @return 更新后的User实体
     */
    public User updateUserFromDTO(User user, UserDTO userDTO) {
        if (user == null || userDTO == null) {
            return user;
        }

        if (StringUtils.isNotBlank(userDTO.getName())) {
            user.setName(userDTO.getName());
        }
        if (StringUtils.isNotBlank(userDTO.getAvatarUrl())) {
            user.setAvatarUrl(userDTO.getAvatarUrl());
        }
        if (StringUtils.isNotBlank(userDTO.getTitle())) {
            user.setTitle(userDTO.getTitle());
        }
        if (StringUtils.isNotBlank(userDTO.getInstitution())) {
            user.setInstitution(userDTO.getInstitution());
        }
        if (userDTO.getIsLocked() != null) {
            user.setIsLocked(userDTO.getIsLocked());
        }

        return user;
    }

    /**
     * 创建简化的LoginUserBody（不包含权限信息）
     * 用于需要快速创建用户上下文但不需要完整权限信息的场景
     *
     * @param userId 用户ID
     * @param email 用户邮箱
     * @param name 用户姓名
     * @return 简化的LoginUserBody对象
     */
    public LoginUserBody createSimpleLoginUserBody(Long userId, String email, String name) {
        if (userId == null || StringUtils.isBlank(email)) {
            return null;
        }

        return LoginUserBody.builder()
                .userId(userId)
                .email(email)
                .name(name)
                .isLocked(false)
                .roles(List.of())
                .permissions(Set.of())
                .build();
    }
}

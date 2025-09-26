package hbnu.project.zhiyansecurity.mapper;

import hbnu.project.zhiyanauthservice.model.dto.PermissionDTO;
import hbnu.project.zhiyanauthservice.model.dto.RoleDTO;
import hbnu.project.zhiyanauthservice.model.dto.TokenDTO;
import hbnu.project.zhiyanauthservice.model.dto.UserDTO;
import hbnu.project.zhiyanauthservice.model.entity.Permission;
import hbnu.project.zhiyanauthservice.model.entity.Role;
import hbnu.project.zhiyanauthservice.model.entity.User;
import hbnu.project.zhiyansecurity.context.LoginUserBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 安全映射器管理类
 * 提供统一的实体转换入口，简化各模块间的调用
 *
 * @author ErgouTree
 */
@Component
public class SecurityMapperManager {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private TokenMapper tokenMapper;

    // ==================== 用户相关转换 ====================

    /**
     * User实体转换为UserDTO
     */
    public UserDTO toUserDTO(User user) {
        return userMapper.toUserDTO(user);
    }

    /**
     * User实体转换为LoginUserBody
     */
    public LoginUserBody toLoginUserBody(User user) {
        return userMapper.toLoginUserBody(user);
    }

    /**
     * UserDTO转换为LoginUserBody
     */
    public LoginUserBody toLoginUserBody(UserDTO userDTO) {
        return userMapper.toLoginUserBody(userDTO);
    }

    /**
     * LoginUserBody转换为UserDTO
     */
    public UserDTO toUserDTO(LoginUserBody loginUserBody) {
        return userMapper.toUserDTO(loginUserBody);
    }

    /**
     * User实体列表转换为UserDTO列表
     */
    public List<UserDTO> toUserDTOList(List<User> users) {
        return userMapper.toUserDTOList(users);
    }

    /**
     * 更新User实体的基本信息
     */
    public User updateUserFromDTO(User user, UserDTO userDTO) {
        return userMapper.updateUserFromDTO(user, userDTO);
    }

    /**
     * 创建简化的LoginUserBody
     */
    public LoginUserBody createSimpleLoginUserBody(Long userId, String email, String name) {
        return userMapper.createSimpleLoginUserBody(userId, email, name);
    }

    // ==================== 角色相关转换 ====================

    /**
     * Role实体转换为RoleDTO
     */
    public RoleDTO toRoleDTO(Role role) {
        return roleMapper.toRoleDTO(role);
    }

    /**
     * RoleDTO转换为Role实体
     */
    public Role toRole(RoleDTO roleDTO) {
        return roleMapper.toRole(roleDTO);
    }

    /**
     * Role实体列表转换为RoleDTO列表
     */
    public List<RoleDTO> toRoleDTOList(List<Role> roles) {
        return roleMapper.toRoleDTOList(roles);
    }

    /**
     * RoleDTO列表转换为Role实体列表
     */
    public List<Role> toRoleList(List<RoleDTO> roleDTOs) {
        return roleMapper.toRoleList(roleDTOs);
    }

    /**
     * 更新Role实体的基本信息
     */
    public Role updateRoleFromDTO(Role role, RoleDTO roleDTO) {
        return roleMapper.updateRoleFromDTO(role, roleDTO);
    }

    /**
     * 验证RoleDTO的必要字段
     */
    public boolean validateRoleDTO(RoleDTO roleDTO) {
        return roleMapper.validateRoleDTO(roleDTO);
    }

    // ==================== 权限相关转换 ====================

    /**
     * Permission实体转换为PermissionDTO
     */
    public PermissionDTO toPermissionDTO(Permission permission) {
        return permissionMapper.toPermissionDTO(permission);
    }

    /**
     * PermissionDTO转换为Permission实体
     */
    public Permission toPermission(PermissionDTO permissionDTO) {
        return permissionMapper.toPermission(permissionDTO);
    }

    /**
     * Permission实体列表转换为PermissionDTO列表
     */
    public List<PermissionDTO> toPermissionDTOList(List<Permission> permissions) {
        return permissionMapper.toPermissionDTOList(permissions);
    }

    /**
     * PermissionDTO列表转换为Permission实体列表
     */
    public List<Permission> toPermissionList(List<PermissionDTO> permissionDTOs) {
        return permissionMapper.toPermissionList(permissionDTOs);
    }

    /**
     * 更新Permission实体的基本信息
     */
    public Permission updatePermissionFromDTO(Permission permission, PermissionDTO permissionDTO) {
        return permissionMapper.updatePermissionFromDTO(permission, permissionDTO);
    }

    /**
     * 验证PermissionDTO的必要字段
     */
    public boolean validatePermissionDTO(PermissionDTO permissionDTO) {
        return permissionMapper.validatePermissionDTO(permissionDTO);
    }

    /**
     * 权限名称列表转换为PermissionDTO列表
     */
    public List<PermissionDTO> fromPermissionNames(List<String> permissionNames) {
        return permissionMapper.fromPermissionNames(permissionNames);
    }

    /**
     * PermissionDTO列表转换为权限名称列表
     */
    public List<String> toPermissionNames(List<PermissionDTO> permissionDTOs) {
        return permissionMapper.toPermissionNames(permissionDTOs);
    }

    // ==================== Token相关转换 ====================

    /**
     * 创建TokenDTO对象
     */
    public TokenDTO createTokenDTO(String accessToken, String refreshToken, Long expiresIn, UserDTO userDTO) {
        return tokenMapper.createTokenDTO(accessToken, refreshToken, expiresIn, userDTO);
    }

    /**
     * 创建TokenDTO对象（使用LoginUserBody）
     */
    public TokenDTO createTokenDTO(String accessToken, String refreshToken, Long expiresIn, LoginUserBody loginUserBody) {
        return tokenMapper.createTokenDTO(accessToken, refreshToken, expiresIn, loginUserBody);
    }

    /**
     * 创建简化的TokenDTO
     */
    public TokenDTO createSimpleTokenDTO(String accessToken, Long expiresIn) {
        return tokenMapper.createSimpleTokenDTO(accessToken, expiresIn);
    }

    /**
     * 验证TokenDTO的必要字段
     */
    public boolean validateTokenDTO(TokenDTO tokenDTO) {
        return tokenMapper.validateTokenDTO(tokenDTO);
    }

    /**
     * 从TokenDTO提取用户信息
     */
    public UserDTO extractUserInfo(TokenDTO tokenDTO) {
        return tokenMapper.extractUserInfo(tokenDTO);
    }

    /**
     * 从TokenDTO提取LoginUserBody
     */
    public LoginUserBody extractLoginUserBody(TokenDTO tokenDTO) {
        return tokenMapper.extractLoginUserBody(tokenDTO);
    }

    /**
     * 创建刷新令牌响应的TokenDTO
     */
    public TokenDTO createRefreshTokenDTO(String newAccessToken, String newRefreshToken, 
                                         Long expiresIn, TokenDTO originalTokenDTO) {
        return tokenMapper.createRefreshTokenDTO(newAccessToken, newRefreshToken, expiresIn, originalTokenDTO);
    }

    /**
     * 检查Token是否包含完整的用户信息
     */
    public boolean hasCompleteUserInfo(TokenDTO tokenDTO) {
        return tokenMapper.hasCompleteUserInfo(tokenDTO);
    }

    // ==================== 综合操作方法 ====================

    /**
     * 创建完整的认证响应对象
     * 
     * @param user 用户实体
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param expiresIn 过期时间
     * @return 完整的TokenDTO对象
     */
    public TokenDTO createAuthResponse(User user, String accessToken, String refreshToken, Long expiresIn) {
        if (user == null) {
            return null;
        }
        
        UserDTO userDTO = userMapper.toUserDTO(user);
        return tokenMapper.createTokenDTO(accessToken, refreshToken, expiresIn, userDTO);
    }

    /**
     * 批量验证实体DTO的有效性
     * 
     * @param userDTO 用户DTO
     * @param roleDTOs 角色DTO列表
     * @param permissionDTOs 权限DTO列表
     * @return 验证结果
     */
    public boolean validateEntities(UserDTO userDTO, List<RoleDTO> roleDTOs, List<PermissionDTO> permissionDTOs) {
        // 验证用户信息
        if (userDTO != null && userDTO.getId() == null) {
            return false;
        }

        // 验证角色信息
        if (roleDTOs != null) {
            for (RoleDTO roleDTO : roleDTOs) {
                if (!roleMapper.validateRoleDTO(roleDTO)) {
                    return false;
                }
            }
        }

        // 验证权限信息
        if (permissionDTOs != null) {
            for (PermissionDTO permissionDTO : permissionDTOs) {
                if (!permissionMapper.validatePermissionDTO(permissionDTO)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 获取所有映射器实例（用于测试或特殊需求）
     */
    public UserMapper getUserMapper() {
        return userMapper;
    }

    public RoleMapper getRoleMapper() {
        return roleMapper;
    }

    public PermissionMapper getPermissionMapper() {
        return permissionMapper;
    }

    public TokenMapper getTokenMapper() {
        return tokenMapper;
    }
}

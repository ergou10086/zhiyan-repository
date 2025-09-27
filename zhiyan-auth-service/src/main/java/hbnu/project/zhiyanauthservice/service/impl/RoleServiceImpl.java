package hbnu.project.zhiyanauthservice.service.impl;

import hbnu.project.zhiyanauthservice.mapper.MapperManager;
import hbnu.project.zhiyanauthservice.model.dto.RoleDTO;
import hbnu.project.zhiyanauthservice.model.entity.Permission;
import hbnu.project.zhiyanauthservice.model.entity.Role;
import hbnu.project.zhiyanauthservice.model.entity.RolePermission;
import hbnu.project.zhiyanauthservice.model.entity.User;
import hbnu.project.zhiyanauthservice.model.entity.UserRole;
import hbnu.project.zhiyanauthservice.repository.PermissionRepository;
import hbnu.project.zhiyanauthservice.repository.RolePermissionRepository;
import hbnu.project.zhiyanauthservice.repository.RoleRepository;
import hbnu.project.zhiyanauthservice.repository.UserRepository;
import hbnu.project.zhiyanauthservice.repository.UserRoleRepository;
import hbnu.project.zhiyanauthservice.service.RoleService;
import hbnu.project.zhiyancommon.domain.R;
import hbnu.project.zhiyancommon.service.RedisService;
import hbnu.project.zhiyancommon.utils.id.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 处理角色管理和分配
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final MapperManager mapperManager;
    private final RedisService redisService;

    // 缓存相关常量
    private static final String USER_ROLES_CACHE_PREFIX = "user:roles:";
    private static final String ROLE_CACHE_PREFIX = "role:";
    private static final String ROLE_PERMISSIONS_CACHE_PREFIX = "role:permissions:";
    private static final long CACHE_EXPIRE_TIME = 1800L; // 30分钟

    @Override
    public R<Set<String>> getUserRoles(Long userId) {
        try {
            if (userId == null) {
                return R.fail("用户ID不能为空");
            }

            // 先从缓存获取
            Set<String> userRoles = getUserRolesFromCache(userId);
            if (userRoles == null) {
                // 缓存未命中，从数据库查询
                List<Role> roles = roleRepository.findAllByUserId(userId);
                userRoles = roles.stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet());
                
                // 缓存用户角色
                cacheUserRoles(userId, userRoles);
            }

            log.debug("获取用户[{}]角色列表，共{}个角色", userId, userRoles.size());
            return R.ok(userRoles);
        } catch (Exception e) {
            log.error("获取用户角色失败: userId={}", userId, e);
            return R.fail("获取用户角色失败");
        }
    }

    @Override
    @Transactional
    public R<Void> assignRolesToUser(Long userId, List<Long> roleIds) {
        try {
            if (userId == null || roleIds == null || roleIds.isEmpty()) {
                return R.fail("用户ID和角色ID列表不能为空");
            }

            // 验证用户是否存在
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return R.fail("用户不存在: " + userId);
            }

            // 验证角色是否存在
            List<Role> roles = roleRepository.findAllById(roleIds);
            if (roles.size() != roleIds.size()) {
                return R.fail("部分角色不存在");
            }

            // 获取用户已有的角色关联
            List<UserRole> existingUserRoles = userRoleRepository.findByUserId(userId);
            Set<Long> existingRoleIds = existingUserRoles.stream()
                    .map(ur -> ur.getRole().getId())
                    .collect(Collectors.toSet());

            // 过滤掉已存在的角色关联
            List<Long> newRoleIds = roleIds.stream()
                    .filter(roleId -> !existingRoleIds.contains(roleId))
                    .collect(Collectors.toList());

            if (newRoleIds.isEmpty()) {
                return R.ok(null, "用户已拥有所有指定角色");
            }

            // 创建新的角色关联
            List<UserRole> newUserRoles = newRoleIds.stream()
                    .map(roleId -> {
                        Role role = roles.stream()
                                .filter(r -> r.getId().equals(roleId))
                                .findFirst()
                                .orElse(null);
                        
                        return UserRole.builder()
                                .id(SnowflakeIdUtil.nextId())
                                .user(user)
                                .role(role)
                                .build();
                    })
                    .collect(Collectors.toList());

            userRoleRepository.saveAll(newUserRoles);

            // 清理相关缓存
            clearUserRolesCache(userId);
            clearUserPermissionsCache(userId);

            log.info("为用户[{}]分配角色成功: {}", userId, newRoleIds);
            return R.ok(null, "角色分配成功");
        } catch (Exception e) {
            log.error("为用户分配角色失败: userId={}, roleIds={}", userId, roleIds, e);
            return R.fail("角色分配失败");
        }
    }

    @Override
    @Transactional
    public R<Void> removeRolesFromUser(Long userId, List<Long> roleIds) {
        try {
            if (userId == null || roleIds == null || roleIds.isEmpty()) {
                return R.fail("用户ID和角色ID列表不能为空");
            }

            // 删除用户角色关联
            int deletedCount = 0;
            for (Long roleId : roleIds) {
                deletedCount += userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
            }

            // 清理相关缓存
            clearUserRolesCache(userId);
            clearUserPermissionsCache(userId);

            log.info("移除用户[{}]角色成功: {}, 删除了{}条记录", userId, roleIds, deletedCount);
            return R.ok(null, "角色移除成功");
        } catch (Exception e) {
            log.error("移除用户角色失败: userId={}, roleIds={}", userId, roleIds, e);
            return R.fail("角色移除失败");
        }
    }

    @Override
    public R<Page<RoleDTO>> getAllRoles(Pageable pageable) {
        try {
            Page<Role> rolePage = roleRepository.findAll(pageable);
            List<RoleDTO> roleDTOs = mapperManager.convertToRoleDTOList(rolePage.getContent());
            
            Page<RoleDTO> result = new PageImpl<>(roleDTOs, pageable, rolePage.getTotalElements());
            
            log.debug("获取角色列表，页码: {}, 大小: {}, 总数: {}", 
                     pageable.getPageNumber(), pageable.getPageSize(), rolePage.getTotalElements());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return R.fail("获取角色列表失败");
        }
    }

    @Override
    @Transactional
    public R<RoleDTO> createRole(RoleDTO roleDTO) {
        try {
            if (roleDTO == null || !StringUtils.hasText(roleDTO.getName())) {
                return R.fail("角色信息不完整");
            }

            // 检查角色名称是否已存在
            if (roleRepository.existsByName(roleDTO.getName())) {
                return R.fail("角色名称已存在: " + roleDTO.getName());
            }

            // 转换为实体并保存
            Role role = mapperManager.convertFromRoleDTO(roleDTO);
            Role savedRole = roleRepository.save(role);

            RoleDTO result = mapperManager.convertToRoleDTO(savedRole);
            
            // 清理相关缓存
            clearRoleCache(savedRole.getId());
            
            log.info("创建角色成功: {}", savedRole.getName());
            return R.ok(result, "角色创建成功");
        } catch (Exception e) {
            log.error("创建角色失败: {}", roleDTO, e);
            return R.fail("创建角色失败");
        }
    }

    @Override
    @Transactional
    public R<RoleDTO> updateRole(Long roleId, RoleDTO roleDTO) {
        try {
            if (roleId == null || roleDTO == null) {
                return R.fail("角色ID和角色信息不能为空");
            }

            Role existingRole = roleRepository.findById(roleId).orElse(null);
            if (existingRole == null) {
                return R.fail("角色不存在: " + roleId);
            }

            // 如果修改了名称，检查新名称是否已存在
            if (StringUtils.hasText(roleDTO.getName()) && 
                !roleDTO.getName().equals(existingRole.getName())) {
                if (roleRepository.existsByName(roleDTO.getName())) {
                    return R.fail("角色名称已存在: " + roleDTO.getName());
                }
            }

            // 更新角色信息
            mapperManager.updateRole(existingRole, roleDTO);
            Role updatedRole = roleRepository.save(existingRole);

            RoleDTO result = mapperManager.convertToRoleDTO(updatedRole);
            
            // 清理相关缓存
            clearRoleCache(roleId);
            clearAllUserRolesCache();
            clearAllUserPermissionsCache();
            
            log.info("更新角色成功: id={}, name={}", roleId, updatedRole.getName());
            return R.ok(result, "角色更新成功");
        } catch (Exception e) {
            log.error("更新角色失败: id={}, dto={}", roleId, roleDTO, e);
            return R.fail("更新角色失败");
        }
    }

    @Override
    @Transactional
    public R<Void> deleteRole(Long roleId) {
        try {
            if (roleId == null) {
                return R.fail("角色ID不能为空");
            }

            Role role = roleRepository.findById(roleId).orElse(null);
            if (role == null) {
                return R.fail("角色不存在: " + roleId);
            }

            // 检查是否有用户关联了该角色
            List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
            if (!userRoles.isEmpty()) {
                return R.fail("无法删除角色，该角色已被用户使用");
            }

            roleRepository.delete(role);
            
            // 清理相关缓存
            clearRoleCache(roleId);
            clearRolePermissionsCache(roleId);
            
            log.info("删除角色成功: id={}, name={}", roleId, role.getName());
            return R.ok(null, "角色删除成功");
        } catch (Exception e) {
            log.error("删除角色失败: id={}", roleId, e);
            return R.fail("删除角色失败");
        }
    }

    @Override
    @Transactional
    public R<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        try {
            if (roleId == null || permissionIds == null || permissionIds.isEmpty()) {
                return R.fail("角色ID和权限ID列表不能为空");
            }

            // 验证角色是否存在
            Role role = roleRepository.findById(roleId).orElse(null);
            if (role == null) {
                return R.fail("角色不存在: " + roleId);
            }

            // 验证权限是否存在
            List<Permission> permissions = permissionRepository.findAllById(permissionIds);
            if (permissions.size() != permissionIds.size()) {
                return R.fail("部分权限不存在");
            }

            // 获取角色已有的权限关联
            List<RolePermission> existingRolePermissions = rolePermissionRepository.findByRoleId(roleId);
            Set<Long> existingPermissionIds = existingRolePermissions.stream()
                    .map(rp -> rp.getPermission().getId())
                    .collect(Collectors.toSet());

            // 过滤掉已存在的权限关联
            List<Long> newPermissionIds = permissionIds.stream()
                    .filter(permissionId -> !existingPermissionIds.contains(permissionId))
                    .collect(Collectors.toList());

            if (newPermissionIds.isEmpty()) {
                return R.ok(null, "角色已拥有所有指定权限");
            }

            // 创建新的权限关联
            List<RolePermission> newRolePermissions = newPermissionIds.stream()
                    .map(permissionId -> {
                        Permission permission = permissions.stream()
                                .filter(p -> p.getId().equals(permissionId))
                                .findFirst()
                                .orElse(null);
                        
                        return RolePermission.builder()
                                .id(SnowflakeIdUtil.nextId())
                                .role(role)
                                .permission(permission)
                                .build();
                    })
                    .collect(Collectors.toList());

            rolePermissionRepository.saveAll(newRolePermissions);

            // 清理相关缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            log.info("为角色[{}]分配权限成功: {}", roleId, newPermissionIds);
            return R.ok(null, "权限分配成功");
        } catch (Exception e) {
            log.error("为角色分配权限失败: roleId={}, permissionIds={}", roleId, permissionIds, e);
            return R.fail("权限分配失败");
        }
    }

    @Override
    @Transactional
    public R<Void> removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        try {
            if (roleId == null || permissionIds == null || permissionIds.isEmpty()) {
                return R.fail("角色ID和权限ID列表不能为空");
            }

            // 删除角色权限关联
            int deletedCount = 0;
            for (Long permissionId : permissionIds) {
                deletedCount += rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
            }

            // 清理相关缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            log.info("移除角色[{}]权限成功: {}, 删除了{}条记录", roleId, permissionIds, deletedCount);
            return R.ok(null, "权限移除成功");
        } catch (Exception e) {
            log.error("移除角色权限失败: roleId={}, permissionIds={}", roleId, permissionIds, e);
            return R.fail("权限移除失败");
        }
    }

    @Override
    public Role findById(Long roleId) {
        if (roleId == null) {
            return null;
        }

        try {
            // 先从缓存查找
            String cacheKey = ROLE_CACHE_PREFIX + roleId;
            Role cachedRole = redisService.getCacheObject(cacheKey);
            
            if (cachedRole != null) {
                return cachedRole;
            }

            // 缓存未命中，从数据库查询
            Role role = roleRepository.findById(roleId).orElse(null);
            
            if (role != null) {
                // 缓存角色信息
                redisService.setCacheObject(cacheKey, role, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            }

            return role;
        } catch (Exception e) {
            log.error("根据ID查找角色失败: id={}", roleId, e);
            // 如果缓存出错，直接从数据库查询
            return roleRepository.findById(roleId).orElse(null);
        }
    }

    @Override
    public Role findByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }

        try {
            return roleRepository.findByName(name).orElse(null);
        } catch (Exception e) {
            log.error("根据名称查找角色失败: name={}", name, e);
            return null;
        }
    }

    /**
     * 从缓存获取用户角色
     */
    private Set<String> getUserRolesFromCache(Long userId) {
        try {
            String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
            return redisService.getCacheObject(cacheKey);
        } catch (Exception e) {
            log.warn("从缓存获取用户角色失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 缓存用户角色
     */
    private void cacheUserRoles(Long userId, Set<String> roles) {
        try {
            String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
            redisService.setCacheObject(cacheKey, roles, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存用户角色失败: userId={}", userId, e);
        }
    }

    /**
     * 清理角色缓存
     */
    private void clearRoleCache(Long roleId) {
        try {
            String cacheKey = ROLE_CACHE_PREFIX + roleId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理角色缓存失败: roleId={}", roleId, e);
        }
    }

    /**
     * 清理用户角色缓存
     */
    private void clearUserRolesCache(Long userId) {
        try {
            String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理用户角色缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 清理所有用户角色缓存
     */
    private void clearAllUserRolesCache() {
        try {
            Collection<String> keys = redisService.keys(USER_ROLES_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisService.deleteObject(keys);
            }
        } catch (Exception e) {
            log.warn("清理所有用户角色缓存失败", e);
        }
    }

    /**
     * 清理角色权限缓存
     */
    private void clearRolePermissionsCache(Long roleId) {
        try {
            String cacheKey = ROLE_PERMISSIONS_CACHE_PREFIX + roleId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理角色权限缓存失败: roleId={}", roleId, e);
        }
    }

    /**
     * 清理用户权限缓存
     */
    private void clearUserPermissionsCache(Long userId) {
        try {
            String cacheKey = "user:permissions:" + userId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理用户权限缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 清理所有用户权限缓存
     */
    private void clearAllUserPermissionsCache() {
        try {
            Collection<String> keys = redisService.keys("user:permissions:*");
            if (keys != null && !keys.isEmpty()) {
                redisService.deleteObject(keys);
            }
        } catch (Exception e) {
            log.warn("清理所有用户权限缓存失败", e);
        }
    }
}

package hbnu.project.zhiyanauthservice.service.impl;

import hbnu.project.zhiyanauthservice.mapper.MapperManager;
import hbnu.project.zhiyanauthservice.model.dto.RoleDTO;
import hbnu.project.zhiyanauthservice.model.entity.Permission;
import hbnu.project.zhiyanauthservice.model.entity.Role;
import hbnu.project.zhiyanauthservice.model.entity.RolePermission;
import hbnu.project.zhiyanauthservice.model.entity.User;
import hbnu.project.zhiyanauthservice.model.entity.UserRole;
import hbnu.project.zhiyanauthservice.model.enums.PermissionModule;
import hbnu.project.zhiyanauthservice.model.enums.RoleTemplate;
import hbnu.project.zhiyanauthservice.repository.PermissionRepository;
import hbnu.project.zhiyanauthservice.repository.RolePermissionRepository;
import hbnu.project.zhiyanauthservice.repository.RoleRepository;
import hbnu.project.zhiyanauthservice.repository.UserRepository;
import hbnu.project.zhiyanauthservice.repository.UserRoleRepository;
import hbnu.project.zhiyanauthservice.service.RoleService;
import hbnu.project.zhiyanauthservice.utils.PermissionAssignmentUtil;
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
    private final PermissionAssignmentUtil permissionAssignmentUtil;

    // 缓存相关常量
    private static final String USER_ROLES_CACHE_PREFIX = "user:roles:";
    private static final String ROLE_CACHE_PREFIX = "role:";
    private static final String ROLE_PERMISSIONS_CACHE_PREFIX = "role:permissions:";
    private static final long CACHE_EXPIRE_TIME = 1800L; // 30分钟


    /**
     * 获取用户拥有的所有角色名称集合
     *
     * @param userId 用户ID（不能为空，作为缓存键核心标识）
     * @return R<Set<String>> - 成功返回角色名称集合（如["ADMIN", "USER"]）；失败返回错误信息
     */
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
                // 提取角色名称，转换为Set集合
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


    /**
     * 为用户分配角色（多对多关联）
     * 核心逻辑：验证用户/角色存在性 → 过滤已存在关联 → 新增关联关系 → 清理缓存
     *
     * @param userId  被分配角色的用户ID
     * @param roleIds 待分配的角色ID列表（不能为空）
     * @return R<Void> - 成功返回"角色分配成功"；失败返回错误信息（如用户不存在、部分角色不存在）
     */
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

            // 查询用户已有的角色关联，避免重复分配
            List<UserRole> existingUserRoles = userRoleRepository.findByUserId(userId);
            // 提取已关联的角色ID，存入Set便于快速判断
            Set<Long> existingRoleIds = existingUserRoles.stream()
                    .map(ur -> ur.getRole().getId())
                    .collect(Collectors.toSet());

            // 过滤掉已存在的角色ID，只保留需要新增的角色
            List<Long> newRoleIds = roleIds.stream()
                    .filter(roleId -> !existingRoleIds.contains(roleId))
                    .collect(Collectors.toList());

            if (newRoleIds.isEmpty()) {
                return R.ok(null, "用户已拥有所有指定角色");
            }

            // 构建新的用户-角色关联实体
            List<UserRole> newUserRoles = newRoleIds.stream()
                    .map(roleId -> {
                        // 从已查询的角色列表中匹配当前角色ID
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

            // 写入数据库
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


    /**
     * 从用户身上移除角色（解除多对多关联）
     *
     * @param userId  被移除角色的用户ID
     * @param roleIds 待移除的角色ID列表（不能为空）
     * @return R<Void> - 成功返回"角色移除成功"；失败返回错误信息
     */
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


    /**
     * 分页查询所有角色（管理员后台角色管理功能常用）
     * 支持分页、排序，返回DTO避免暴露数据库实体细节
     *
     * @param pageable 分页参数（包含页码、页大小、排序规则，如按角色名称升序）
     * @return R<Page<RoleDTO>> - 分页后的角色DTO列表，包含总条数、总页数等分页信息
     */
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


    /**
     * 创建新角色
     *
     * @param roleDTO 角色DTO（包含角色名称、描述等前端传入信息）
     * @return R<RoleDTO> - 成功返回创建后的角色DTO（含数据库生成的ID）；失败返回错误信息
     */
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

            // 实体转换为DTO，返回给前端
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


    /**
     * 更新已有角色信息
     *
     * @param roleId   待更新的角色ID
     * @param roleDTO  新的角色信息（DTO）
     * @return R<RoleDTO> - 成功返回更新后的角色DTO；失败返回错误信息
     */
    @Override
    @Transactional
    public R<RoleDTO> updateRole(Long roleId, RoleDTO roleDTO) {
        try {
            if (roleId == null || roleDTO == null) {
                return R.fail("角色ID和角色信息不能为空");
            }

            // 校验角色是否存在
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

            // 更新角色信息，然后写入
            mapperManager.updateRole(existingRole, roleDTO);
            Role updatedRole = roleRepository.save(existingRole);

            // 实体转换为DTO返回
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


    /**
     * 删除角色
     * 已被用户关联的角色不允许删除，避免权限失控
     *
     * @param roleId 待删除的角色ID
     * @return R<Void> - 成功返回"角色删除成功"；失败返回错误信息（如角色不存在、已被使用）
     */
    @Override
    @Transactional
    public R<Void> deleteRole(Long roleId) {
        try {
            if (roleId == null) {
                return R.fail("角色ID不能为空");
            }

            // 校验角色是否存在
            Role role = roleRepository.findById(roleId).orElse(null);
            if (role == null) {
                return R.fail("角色不存在: " + roleId);
            }

            // 检查是否有用户关联了该角色
            List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
            if (!userRoles.isEmpty()) {
                return R.fail("无法删除角色，该角色已被用户使用");
            }

            // 执行角色删除操作
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


    /**
     * 为角色分配权限（角色-权限多对多关联）
     * 通过角色间接管理用户权限，实现权限的批量分配与回收
     *
     * @param roleId        被分配权限的角色ID
     * @param permissionIds 待分配的权限ID列表（不能为空）
     * @return R<Void> - 成功返回"权限分配成功"；失败返回错误信息（如角色不存在、部分权限无效）
     */
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
                        // 从已查询的权限列表中匹配当前权限ID
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


    /**
     * 从角色中移除权限（解除角色-权限关联）
     *
     * @param roleId        被移除权限的角色ID
     * @param permissionIds 待移除的权限ID列表（不能为空）
     * @return R<Void> - 成功返回"权限移除成功"；失败返回错误信息
     */
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


    /**
     * 根据角色ID查询角色实体（内部使用，如权限分配前的校验）
     *
     * @param roleId 角色ID
     * @return Role - 成功返回角色实体；不存在或异常时返回null
     */
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


    /**
     * 根据角色名称查询角色实体（如权限校验时的角色名称匹配）
     * 注：未使用缓存，适用于角色名称不频繁变更的场景
     *
     * @param name 角色名称（不能为空）
     * @return Role - 成功返回角色实体；不存在或异常时返回null
     */
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
     * 从Redis缓存中获取用户的角色列表
     * 封装缓存查询逻辑，统一处理异常，避免业务方法重复编写缓存代码
     *
     * @param userId 用户ID（缓存键核心标识）
     * @return Set<String> - 缓存命中返回角色名称集合；未命中或异常返回null（触发数据库查询）
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
     * 将用户角色列表存入Redis缓存
     * 封装缓存写入逻辑，统一设置过期时间，确保缓存数据时效性
     *
     * @param userId 用户ID（缓存键标识）
     * @param roles 需缓存的角色名称集合（从数据库查询的最新数据）
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
     * 清理指定角色ID的缓存
     * 用于角色信息变更（如名称修改、删除）后，确保缓存数据与数据库一致
     *
     * @param roleId 角色ID（缓存键核心标识）
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
     * 清理指定用户的角色缓存
     * 用于用户角色变更（如分配/移除角色）后，强制重新加载最新角色列表
     *
     * @param userId 用户ID（缓存键核心标识）
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
     * 批量清理所有用户的角色缓存
     * 用于全局角色信息变更（如角色名称修改）后，确保所有用户的角色列表同步更新
     */
    private void clearAllUserRolesCache() {
        try {
            // 模糊匹配所有用户角色缓存键（如"user:roles:*"）
            Collection<String> keys = redisService.keys(USER_ROLES_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisService.deleteObject(keys);
            }
        } catch (Exception e) {
            log.warn("清理所有用户角色缓存失败", e);
        }
    }


    /**
     * 清理指定角色的权限缓存
     * 用于角色权限变更（如分配/移除权限）后，确保角色的权限列表重新加载
     *
     * @param roleId 角色ID（缓存键核心标识）
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
     * 清理指定用户的权限缓存
     * 用于用户角色变更（间接影响权限）后，确保用户的权限列表重新加载
     *
     * @param userId 用户ID（缓存键核心标识）
     */
    private void clearUserPermissionsCache(Long userId) {
        try {
            // 注意：此处直接使用"user:permissions:"前缀（与权限服务保持一致）
            String cacheKey = "user:permissions:" + userId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理用户权限缓存失败: userId={}", userId, e);
        }
    }


    /**
     * 批量清理所有用户的权限缓存
     * 用于全局权限变更（如角色权限调整）后，确保所有用户的权限列表同步更新
     */
    private void clearAllUserPermissionsCache() {
        try {
            // 模糊匹配所有用户权限缓存键（与权限服务的缓存键规则一致）
            Collection<String> keys = redisService.keys("user:permissions:*");
            if (keys != null && !keys.isEmpty()) {
                redisService.deleteObject(keys);
            }
        } catch (Exception e) {
            log.warn("清理所有用户权限缓存失败", e);
        }
    }

    // ========== 批量权限分配方法实现 ==========

    /**
     * 为角色分配权限模块
     */
    @Override
    @Transactional
    public R<Integer> assignPermissionModule(Long roleId, PermissionModule permissionModule) {
        try {
            Role role = findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            int assignedCount = permissionAssignmentUtil.assignPermissionModule(role, permissionModule);
            
            // 清理相关缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            return R.ok(assignedCount, String.format("成功为角色 '%s' 分配权限模块 '%s'，共分配 %d 个权限", 
                    role.getName(), permissionModule.getModuleName(), assignedCount));
        } catch (Exception e) {
            log.error("为角色分配权限模块失败: roleId={}, module={}", roleId, permissionModule, e);
            return R.fail("权限模块分配失败: " + e.getMessage());
        }
    }


    /**
     * 为角色分配多个权限模块
     */
    @Override
    @Transactional
    public R<Integer> assignPermissionModules(Long roleId, List<PermissionModule> permissionModules) {
        try {
            Role role = findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            int totalAssigned = permissionAssignmentUtil.assignPermissionModules(role, permissionModules);

            // 清理相关缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            String moduleNames = permissionModules.stream()
                    .map(PermissionModule::getModuleName)
                    .collect(Collectors.joining(", "));

            return R.ok(totalAssigned, String.format("成功为角色 '%s' 分配权限模块 [%s]，共分配 %d 个权限",
                    role.getName(), moduleNames, totalAssigned));
        } catch (Exception e) {
            log.error("为角色分配多个权限模块失败: roleId={}, modules={}", roleId, permissionModules, e);
            return R.fail("权限模块批量分配失败: " + e.getMessage());
        }
    }


    /**
     * 根据角色模板创建角色并分配权限
     */
    @Override
    @Transactional
    public R<RoleDTO> createRoleFromTemplate(RoleTemplate roleTemplate, String roleName) {
        try {
            // 使用模板名称或自定义名称
            String finalRoleName = StringUtils.hasText(roleName) ? roleName : roleTemplate.getRoleName();
            
            // 检查角色名称是否已存在
            if (roleRepository.existsByName(finalRoleName)) {
                return R.fail("角色名称已存在: " + finalRoleName);
            }

            // 创建角色
            Role role = Role.builder()
                    .id(SnowflakeIdUtil.nextId())
                    .name(finalRoleName)
                    .description(roleTemplate.getDescription())
                    .build();
            
            role = roleRepository.save(role);

            // 应用权限模板
            int assignedCount = permissionAssignmentUtil.assignRoleTemplate(role, roleTemplate);

            RoleDTO roleDTO = mapperManager.convertToRoleDTO(role);
            
            log.info("成功创建角色模板: {} -> {}, 分配权限: {}", 
                    roleTemplate.getRoleName(), finalRoleName, assignedCount);

            return R.ok(roleDTO, String.format("成功创建角色 '%s' 并分配 %d 个权限", 
                    finalRoleName, assignedCount));
        } catch (Exception e) {
            log.error("根据模板创建角色失败: template={}, roleName={}", roleTemplate, roleName, e);
            return R.fail("角色模板创建失败: " + e.getMessage());
        }
    }


    /**
     * 为现有角色应用角色模板
     */
    @Override
    @Transactional
    public R<Integer> applyRoleTemplate(Long roleId, RoleTemplate roleTemplate, boolean resetMode) {
        try {
            Role role = findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            int assignedCount;
            if (resetMode) {
                // 重置模式：清空现有权限后重新分配
                assignedCount = permissionAssignmentUtil.resetRolePermissions(role, roleTemplate);
            } else {
                // 增量模式：在现有权限基础上添加
                assignedCount = permissionAssignmentUtil.assignRoleTemplate(role, roleTemplate);
            }

            // 清理相关缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            String mode = resetMode ? "重置" : "增量";
            return R.ok(assignedCount, String.format("成功以%s模式为角色 '%s' 应用模板 '%s'，分配 %d 个权限",
                    mode, role.getName(), roleTemplate.getRoleName(), assignedCount));
        } catch (Exception e) {
            log.error("应用角色模板失败: roleId={}, template={}, resetMode={}", roleId, roleTemplate, resetMode, e);
            return R.fail("角色模板应用失败: " + e.getMessage());
        }
    }


    /**
     * 移除角色的权限模块
     */
    @Override
    @Transactional
    public R<Integer> removePermissionModule(Long roleId, PermissionModule permissionModule) {
        try {
            Role role = findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            int removedCount = permissionAssignmentUtil.removePermissionModule(role, permissionModule);
            
            // 清理相关缓存
            clearRolePermissionsCache(roleId);
            clearAllUserPermissionsCache();

            return R.ok(removedCount, String.format("成功从角色 '%s' 移除权限模块 '%s'，共移除 %d 个权限", 
                    role.getName(), permissionModule.getModuleName(), removedCount));
        } catch (Exception e) {
            log.error("移除角色权限模块失败: roleId={}, module={}", roleId, permissionModule, e);
            return R.fail("权限模块移除失败: " + e.getMessage());
        }
    }


    /**
     * 获取角色权限统计信息
     */
    @Override
    public R<PermissionAssignmentUtil.PermissionStatistics> getRolePermissionStatistics(Long roleId) {
        try {
            Role role = findById(roleId);
            if (role == null) {
                return R.fail("角色不存在");
            }

            PermissionAssignmentUtil.PermissionStatistics statistics = 
                    permissionAssignmentUtil.getPermissionStatistics(role);
            
            return R.ok(statistics, "成功获取角色权限统计信息");
        } catch (Exception e) {
            log.error("获取角色权限统计失败: roleId={}", roleId, e);
            return R.fail("获取权限统计失败: " + e.getMessage());
        }
    }


    /**
     * 初始化系统权限数据
     */
    @Override
    @Transactional
    public R<Void> initializeSystemPermissions() {
        try {
            permissionAssignmentUtil.initializeSystemPermissions();
            return R.ok(null, "系统权限初始化完成");
        } catch (Exception e) {
            log.error("初始化系统权限失败", e);
            return R.fail("系统权限初始化失败: " + e.getMessage());
        }
    }
}

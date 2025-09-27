package hbnu.project.zhiyanauthservice.service.impl;

import hbnu.project.zhiyanauthservice.mapper.MapperManager;
import hbnu.project.zhiyanauthservice.model.dto.PermissionDTO;
import hbnu.project.zhiyanauthservice.model.entity.Permission;
import hbnu.project.zhiyanauthservice.repository.PermissionRepository;
import hbnu.project.zhiyanauthservice.service.PermissionService;
import hbnu.project.zhiyancommon.domain.R;
import hbnu.project.zhiyancommon.exception.ServiceException;
import hbnu.project.zhiyancommon.service.RedisService;
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
 * 权限服务实现类
 * 处理权限管理和验证
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final MapperManager mapperManager;
    private final RedisService redisService;

    // 缓存相关常量
    private static final String USER_PERMISSIONS_CACHE_PREFIX = "user:permissions:";
    private static final String PERMISSION_CACHE_PREFIX = "permission:";
    private static final long CACHE_EXPIRE_TIME = 1800L; // 30分钟

    @Override
    public R<Boolean> hasPermission(Long userId, String permission) {
        try {
            if (userId == null || !StringUtils.hasText(permission)) {
                return R.ok(false);
            }

            // 先从缓存获取用户权限
            Set<String> userPermissions = getUserPermissionsFromCache(userId);
            if (userPermissions == null) {
                // 缓存未命中，从数据库查询
                List<Permission> permissions = permissionRepository.findAllByUserId(userId);
                userPermissions = permissions.stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet());
                
                // 缓存用户权限
                cacheUserPermissions(userId, userPermissions);
            }

            boolean hasPermission = userPermissions.contains(permission);
            log.debug("检查用户[{}]是否拥有权限[{}]: {}", userId, permission, hasPermission);
            
            return R.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查用户权限失败: userId={}, permission={}", userId, permission, e);
            return R.fail("权限检查失败");
        }
    }

    @Override
    public R<Set<String>> getUserPermissions(Long userId) {
        try {
            if (userId == null) {
                return R.fail("用户ID不能为空");
            }

            // 先从缓存获取
            Set<String> userPermissions = getUserPermissionsFromCache(userId);
            if (userPermissions == null) {
                // 缓存未命中，从数据库查询
                List<Permission> permissions = permissionRepository.findAllByUserId(userId);
                userPermissions = permissions.stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet());
                
                // 缓存用户权限
                cacheUserPermissions(userId, userPermissions);
            }

            log.debug("获取用户[{}]权限列表，共{}个权限", userId, userPermissions.size());
            return R.ok(userPermissions);
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}", userId, e);
            return R.fail("获取用户权限失败");
        }
    }

    @Override
    public R<Boolean> hasAnyPermission(Long userId, List<String> permissions) {
        try {
            if (userId == null || permissions == null || permissions.isEmpty()) {
                return R.ok(false);
            }

            // 获取用户所有权限
            R<Set<String>> userPermissionsResult = getUserPermissions(userId);
            if (!R.isSuccess(userPermissionsResult)) {
                return R.fail("获取用户权限失败");
            }

            Set<String> userPermissions = userPermissionsResult.getData();
            
            // 检查是否拥有任一权限
            boolean hasAny = permissions.stream()
                    .anyMatch(userPermissions::contains);

            log.debug("检查用户[{}]是否拥有权限列表{}中的任一权限: {}", userId, permissions, hasAny);
            return R.ok(hasAny);
        } catch (Exception e) {
            log.error("批量检查用户权限失败: userId={}, permissions={}", userId, permissions, e);
            return R.fail("批量权限检查失败");
        }
    }

    @Override
    public R<Page<PermissionDTO>> getAllPermissions(Pageable pageable) {
        try {
            Page<Permission> permissionPage = permissionRepository.findAll(pageable);
            List<PermissionDTO> permissionDTOs = mapperManager.convertToPermissionDTOList(permissionPage.getContent());
            
            Page<PermissionDTO> result = new PageImpl<>(permissionDTOs, pageable, permissionPage.getTotalElements());
            
            log.debug("获取权限列表，页码: {}, 大小: {}, 总数: {}", 
                     pageable.getPageNumber(), pageable.getPageSize(), permissionPage.getTotalElements());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return R.fail("获取权限列表失败");
        }
    }

    @Override
    @Transactional
    public R<PermissionDTO> createPermission(PermissionDTO permissionDTO) {
        try {
            if (permissionDTO == null || !StringUtils.hasText(permissionDTO.getName())) {
                return R.fail("权限信息不完整");
            }

            // 检查权限名称是否已存在
            if (permissionRepository.existsByName(permissionDTO.getName())) {
                return R.fail("权限名称已存在: " + permissionDTO.getName());
            }

            // 转换为实体并保存
            Permission permission = mapperManager.convertFromPermissionDTO(permissionDTO);
            Permission savedPermission = permissionRepository.save(permission);

            PermissionDTO result = mapperManager.convertToPermissionDTO(savedPermission);
            
            // 清理相关缓存
            clearPermissionCache(savedPermission.getId());
            
            log.info("创建权限成功: {}", savedPermission.getName());
            return R.ok(result, "权限创建成功");
        } catch (Exception e) {
            log.error("创建权限失败: {}", permissionDTO, e);
            return R.fail("创建权限失败");
        }
    }

    @Override
    @Transactional
    public R<PermissionDTO> updatePermission(Long permissionId, PermissionDTO permissionDTO) {
        try {
            if (permissionId == null || permissionDTO == null) {
                return R.fail("权限ID和权限信息不能为空");
            }

            Permission existingPermission = permissionRepository.findById(permissionId)
                    .orElse(null);
            
            if (existingPermission == null) {
                return R.fail("权限不存在: " + permissionId);
            }

            // 如果修改了名称，检查新名称是否已存在
            if (StringUtils.hasText(permissionDTO.getName()) && 
                !permissionDTO.getName().equals(existingPermission.getName())) {
                if (permissionRepository.existsByName(permissionDTO.getName())) {
                    return R.fail("权限名称已存在: " + permissionDTO.getName());
                }
            }

            // 更新权限信息
            mapperManager.updatePermission(existingPermission, permissionDTO);
            Permission updatedPermission = permissionRepository.save(existingPermission);

            PermissionDTO result = mapperManager.convertToPermissionDTO(updatedPermission);
            
            // 清理相关缓存
            clearPermissionCache(permissionId);
            clearAllUserPermissionsCache();
            
            log.info("更新权限成功: id={}, name={}", permissionId, updatedPermission.getName());
            return R.ok(result, "权限更新成功");
        } catch (Exception e) {
            log.error("更新权限失败: id={}, dto={}", permissionId, permissionDTO, e);
            return R.fail("更新权限失败");
        }
    }

    @Override
    @Transactional
    public R<Void> deletePermission(Long permissionId) {
        try {
            if (permissionId == null) {
                return R.fail("权限ID不能为空");
            }

            Permission permission = permissionRepository.findById(permissionId)
                    .orElse(null);
            
            if (permission == null) {
                return R.fail("权限不存在: " + permissionId);
            }

            // 检查是否有角色关联了该权限
            if (permission.getRolePermissions() != null && !permission.getRolePermissions().isEmpty()) {
                return R.fail("无法删除权限，该权限已被角色使用");
            }

            permissionRepository.delete(permission);
            
            // 清理相关缓存
            clearPermissionCache(permissionId);
            clearAllUserPermissionsCache();
            
            log.info("删除权限成功: id={}, name={}", permissionId, permission.getName());
            return R.ok(null, "权限删除成功");
        } catch (Exception e) {
            log.error("删除权限失败: id={}", permissionId, e);
            return R.fail("删除权限失败");
        }
    }

    @Override
    public Permission findById(Long permissionId) {
        if (permissionId == null) {
            return null;
        }

        try {
            // 先从缓存查找
            String cacheKey = PERMISSION_CACHE_PREFIX + permissionId;
            Permission cachedPermission = redisService.getCacheObject(cacheKey);
            
            if (cachedPermission != null) {
                return cachedPermission;
            }

            // 缓存未命中，从数据库查询
            Permission permission = permissionRepository.findById(permissionId).orElse(null);
            
            if (permission != null) {
                // 缓存权限信息
                redisService.setCacheObject(cacheKey, permission, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            }

            return permission;
        } catch (Exception e) {
            log.error("根据ID查找权限失败: id={}", permissionId, e);
            // 如果缓存出错，直接从数据库查询
            return permissionRepository.findById(permissionId).orElse(null);
        }
    }

    /**
     * 从缓存获取用户权限
     */
    private Set<String> getUserPermissionsFromCache(Long userId) {
        try {
            String cacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
            return redisService.getCacheObject(cacheKey);
        } catch (Exception e) {
            log.warn("从缓存获取用户权限失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 缓存用户权限
     */
    private void cacheUserPermissions(Long userId, Set<String> permissions) {
        try {
            String cacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
            redisService.setCacheObject(cacheKey, permissions, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存用户权限失败: userId={}", userId, e);
        }
    }

    /**
     * 清理权限缓存
     */
    private void clearPermissionCache(Long permissionId) {
        try {
            String cacheKey = PERMISSION_CACHE_PREFIX + permissionId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理权限缓存失败: permissionId={}", permissionId, e);
        }
    }

    /**
     * 清理所有用户权限缓存
     */
    private void clearAllUserPermissionsCache() {
        try {
            // 清理用户权限相关的缓存
            Collection<String> keys = redisService.keys(USER_PERMISSIONS_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisService.deleteObject(keys);
            }
        } catch (Exception e) {
            log.warn("清理用户权限缓存失败", e);
        }
    }
}

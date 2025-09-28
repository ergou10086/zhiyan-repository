package hbnu.project.zhiyanauthservice.service;

import hbnu.project.zhiyanauthservice.model.dto.RoleDTO;
import hbnu.project.zhiyanauthservice.model.entity.Role;
import hbnu.project.zhiyanauthservice.model.enums.PermissionModule;
import hbnu.project.zhiyanauthservice.model.enums.RoleTemplate;
import hbnu.project.zhiyanauthservice.utils.PermissionAssignmentUtil;
import hbnu.project.zhiyancommon.domain.R;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * 角色服务接口
 * 处理角色管理和分配
 *
 * @author ErgouTree
 */
public interface RoleService {

    /**
     * 获取用户所有角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    R<Set<String>> getUserRoles(Long userId);

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 分配结果
     */
    R<Void> assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 移除用户角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 移除结果
     */
    R<Void> removeRolesFromUser(Long userId, List<Long> roleIds);

    /**
     * 获取所有角色列表
     *
     * @param pageable 分页参数
     * @return 角色列表
     */
    R<Page<RoleDTO>> getAllRoles(Pageable pageable);

    /**
     * 创建角色
     *
     * @param roleDTO 角色信息
     * @return 创建结果
     */
    R<RoleDTO> createRole(RoleDTO roleDTO);

    /**
     * 更新角色
     *
     * @param roleId 角色ID
     * @param roleDTO 角色信息
     * @return 更新结果
     */
    R<RoleDTO> updateRole(Long roleId, RoleDTO roleDTO);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 删除结果
     */
    R<Void> deleteRole(Long roleId);

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 分配结果
     */
    R<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 移除角色权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 移除结果
     */
    R<Void> removePermissionsFromRole(Long roleId, List<Long> permissionIds);

    /**
     * 根据ID查找角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    Role findById(Long roleId);

    /**
     * 根据名称查找角色
     *
     * @param name 角色名称
     * @return 角色信息
     */
    Role findByName(String name);

    // ========== 批量权限分配方法 ==========

    /**
     * 为角色分配权限模块
     *
     * @param roleId           角色ID
     * @param permissionModule 权限模块
     * @return 分配结果
     */
    R<Integer> assignPermissionModule(Long roleId, PermissionModule permissionModule);

    /**
     * 为角色分配多个权限模块
     *
     * @param roleId            角色ID
     * @param permissionModules 权限模块列表
     * @return 分配结果
     */
    R<Integer> assignPermissionModules(Long roleId, List<PermissionModule> permissionModules);

    /**
     * 根据角色模板创建角色并分配权限
     *
     * @param roleTemplate 角色模板
     * @param roleName     自定义角色名称（可选，为空时使用模板名称）
     * @return 创建结果
     */
    R<RoleDTO> createRoleFromTemplate(RoleTemplate roleTemplate, String roleName);

    /**
     * 为现有角色应用角色模板
     *
     * @param roleId       角色ID
     * @param roleTemplate 角色模板
     * @param resetMode    是否重置模式（true：清空现有权限后应用，false：在现有权限基础上添加）
     * @return 应用结果
     */
    R<Integer> applyRoleTemplate(Long roleId, RoleTemplate roleTemplate, boolean resetMode);

    /**
     * 移除角色的权限模块
     *
     * @param roleId           角色ID
     * @param permissionModule 权限模块
     * @return 移除结果
     */
    R<Integer> removePermissionModule(Long roleId, PermissionModule permissionModule);

    /**
     * 获取角色权限统计信息
     *
     * @param roleId 角色ID
     * @return 权限统计信息
     */
    R<PermissionAssignmentUtil.PermissionStatistics> getRolePermissionStatistics(Long roleId);

    /**
     * 初始化系统权限数据
     *
     * @return 初始化结果
     */
    R<Void> initializeSystemPermissions();
}

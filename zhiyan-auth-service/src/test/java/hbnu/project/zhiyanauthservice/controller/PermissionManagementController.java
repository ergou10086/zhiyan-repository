package hbnu.project.zhiyanauthservice.controller;

import hbnu.project.zhiyanauthservice.model.dto.RoleDTO;
import hbnu.project.zhiyanauthservice.model.enums.PermissionModule;
import hbnu.project.zhiyanauthservice.service.RoleService;
import hbnu.project.zhiyanauthservice.utils.PermissionAssignmentUtil;
import hbnu.project.zhiyancommon.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理控制器
 * 提供批量权限分配和角色模板管理功能
 *
 * @author asddjv
 */
@Tag(name = "权限管理", description = "批量权限分配和角色模板管理")
@RestController
@RequestMapping("/api/permission-management")
@RequiredArgsConstructor
public class PermissionManagementController {

    private final RoleService roleService;

    // ========== 权限模块管理 ==========

    /**
     * 获取所有权限模块列表
     */
    @Operation(summary = "获取权限模块列表", description = "获取系统中所有可用的权限模块")
    @GetMapping("/modules")
    public R<List<PermissionModuleInfo>> getPermissionModules() {
        List<PermissionModuleInfo> modules = Arrays.stream(PermissionModule.values())
                .map(module -> PermissionModuleInfo.builder()
                        .moduleCode(module.name())
                        .moduleName(module.getModuleName())
                        .description(module.getDescription())
                        .permissionCount(module.getPermissionCount())
                        .permissions(module.getPermissionStrings())
                        .build())
                .collect(Collectors.toList());

        return R.ok(modules, "成功获取权限模块列表");
    }

    /**
     * 为角色分配权限模块
     */
    @Operation(summary = "分配权限模块", description = "为指定角色分配一个权限模块的所有权限")
    @PostMapping("/roles/{roleId}/assign-module")
    public R<Integer> assignPermissionModule(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "权限模块") @RequestParam PermissionModule module) {
        return roleService.assignPermissionModule(roleId, module);
    }

    /**
     * 为角色分配多个权限模块
     */
    @Operation(summary = "批量分配权限模块", description = "为指定角色分配多个权限模块的所有权限")
    @PostMapping("/roles/{roleId}/assign-modules")
    public R<Integer> assignPermissionModules(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "权限模块列表") @RequestBody List<PermissionModule> modules) {
        return roleService.assignPermissionModules(roleId, modules);
    }

    /**
     * 移除角色的权限模块
     */
    @Operation(summary = "移除权限模块", description = "从指定角色移除一个权限模块的所有权限")
    @DeleteMapping("/roles/{roleId}/remove-module")
    public R<Integer> removePermissionModule(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "权限模块") @RequestParam PermissionModule module) {
        return roleService.removePermissionModule(roleId, module);
    }

    // ========== 角色模板管理 ==========

    /**
     * 获取所有角色模板列表
     */
    @Operation(summary = "获取角色模板列表", description = "获取系统中所有预定义的角色模板")
    @GetMapping("/role-templates")
    public R<List<RoleTemplateInfo>> getRoleTemplates() {
        List<RoleTemplateInfo> templates = Arrays.stream(RoleTemplate.values())
                .map(template -> RoleTemplateInfo.builder()
                        .templateCode(template.name())
                        .roleCode(template.getRoleCode())
                        .roleName(template.getRoleName())
                        .description(template.getDescription())
                        .moduleCount(template.getModuleCount())
                        .totalPermissions(template.getTotalPermissionCount())
                        .permissionModules(template.getPermissionModules().stream()
                                .map(PermissionModule::getModuleName)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return R.ok(templates, "成功获取角色模板列表");
    }

    /**
     * 根据角色模板创建角色
     */
    @Operation(summary = "根据模板创建角色", description = "使用预定义模板创建角色并自动分配相应权限")
    @PostMapping("/create-role-from-template")
    public R<RoleDTO> createRoleFromTemplate(
            @Parameter(description = "角色模板") @RequestParam RoleTemplate template,
            @Parameter(description = "自定义角色名称（可选）") @RequestParam(required = false) String roleName) {
        return roleService.createRoleFromTemplate(template, roleName);
    }

    /**
     * 为现有角色应用模板
     */
    @Operation(summary = "应用角色模板", description = "为现有角色应用模板权限配置")
    @PostMapping("/roles/{roleId}/apply-template")
    public R<Integer> applyRoleTemplate(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "角色模板") @RequestParam RoleTemplate template,
            @Parameter(description = "是否重置现有权限") @RequestParam(defaultValue = "false") boolean resetMode) {
        return roleService.applyRoleTemplate(roleId, template, resetMode);
    }

    // ========== 权限统计和管理 ==========

    /**
     * 获取角色权限统计
     */
    @Operation(summary = "获取角色权限统计", description = "获取指定角色的权限统计信息")
    @GetMapping("/roles/{roleId}/statistics")
    public R<PermissionAssignmentUtil.PermissionStatistics> getRolePermissionStatistics(
            @Parameter(description = "角色ID") @PathVariable Long roleId) {
        return roleService.getRolePermissionStatistics(roleId);
    }

    /**
     * 初始化系统权限
     */
    @Operation(summary = "初始化系统权限", description = "将所有SystemPermission枚举中的权限同步到数据库")
    @PostMapping("/initialize-permissions")
    public R<Void> initializeSystemPermissions() {
        return roleService.initializeSystemPermissions();
    }

    // ========== 数据传输对象 ==========

    /**
     * 权限模块信息
     */
    @lombok.Data
    @lombok.Builder
    public static class PermissionModuleInfo {
        private String moduleCode;
        private String moduleName;
        private String description;
        private int permissionCount;
        private List<String> permissions;
    }

    /**
     * 角色模板信息
     */
    @lombok.Data
    @lombok.Builder
    public static class RoleTemplateInfo {
        private String templateCode;
        private String roleCode;
        private String roleName;
        private String description;
        private int moduleCount;
        private int totalPermissions;
        private List<String> permissionModules;
    }
}

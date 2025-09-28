package hbnu.project.zhiyanauthservice.service.impl;

import hbnu.project.zhiyanauthservice.model.dto.UserDTO;
import hbnu.project.zhiyanauthservice.model.entity.User;
import hbnu.project.zhiyanauthservice.model.form.UserProfileUpdateBody;
import hbnu.project.zhiyanauthservice.repository.RoleRepository;
import hbnu.project.zhiyanauthservice.repository.UserRepository;
import hbnu.project.zhiyanauthservice.repository.UserRoleRepository;
import hbnu.project.zhiyanauthservice.mapper.MapperManager;
import hbnu.project.zhiyanauthservice.service.AuthService;
import hbnu.project.zhiyanauthservice.service.PermissionService;
import hbnu.project.zhiyanauthservice.service.UserService;
import hbnu.project.zhiyanauthservice.service.VerificationCodeService;
import hbnu.project.zhiyancommon.domain.R;
import hbnu.project.zhiyancommon.utils.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


/**
 * 用户服务实现类
 * 提供用户管理、认证、权限等核心功能
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final VerificationCodeService verificationCodeService;
    private final AuthService authService;
    private final PermissionService permissionService;
    private final MapperManager mapperManager;


    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public R<UserDTO> getCurrentUser(Long userId) {
        try {
            Optional<User> optionalUser = userRepository.findByIdWithRolesAndPermissions(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            UserDTO userDTO = mapperManager.convertToUserDTOWithRolesAndPermissions(optionalUser.get());
            return R.ok(userDTO);

        } catch (Exception e) {
            log.error("获取用户信息异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("获取用户信息失败");
        }
    }


    /**
     * 更新用户资料
     *
     * @param userId 用户ID
     * @param updateBody 更新表单
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<UserDTO> updateUserProfile(Long userId, UserProfileUpdateBody updateBody) {
        try {
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();

            // 更新用户信息
            mapperManager.updateUserProfile(user, updateBody);

            user = userRepository.save(user);
            UserDTO userDTO = mapperManager.convertToUserDTO(user);

            log.info("用户资料更新成功 - 用户ID: {}", userId);
            return R.ok(userDTO, "资料更新成功");

        } catch (Exception e) {
            log.error("用户资料更新异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("资料更新失败，请稍后重试");
        }
    }


    /**
     * 分页查询用户列表（管理员功能）
     *
     * @param pageable 分页参数
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    @Override
    public R<Page<UserDTO>> getUserList(Pageable pageable, String keyword) {
        try {
            Page<User> userPage;

            if (StringUtils.isNotBlank(keyword)) {
                userPage = userRepository.findByNameContainingOrEmailContainingAndIsDeletedFalse(
                        keyword, keyword, pageable);
            } else {
                userPage = userRepository.findByIsDeletedFalse(pageable);
            }

            List<UserDTO> userDTOs = mapperManager.convertToUserDTOList(userPage.getContent());

            Page<UserDTO> userDTOPage = new PageImpl<>(userDTOs, pageable, userPage.getTotalElements());
            return R.ok(userDTOPage);

        } catch (Exception e) {
            log.error("查询用户列表异常 - 错误: {}", e.getMessage(), e);
            return R.fail("查询用户列表失败");
        }
    }


    /**
     * 锁定/解锁用户
     *
     * @param userId 用户ID
     * @param isLocked 是否锁定
     * @return 操作结果
     */
    @Override
    @Transactional
    public R<Void> lockUser(Long userId, boolean isLocked) {
        try {
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            user.setIsLocked(isLocked);
            userRepository.save(user);

            String action = isLocked ? "锁定" : "解锁";
            log.info("用户{}成功 - 用户ID: {}", action, userId);
            return R.ok(null, "用户" + action + "成功");

        } catch (Exception e) {
            log.error("用户锁定/解锁异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("操作失败，请稍后重试");
        }
    }


    /**
     * 软删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public R<Void> deleteUser(Long userId) {
        try {
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            user.setIsDeleted(true);
            userRepository.save(user);

            log.info("用户删除成功 - 用户ID: {}", userId);
            return R.ok(null, "用户删除成功");

        } catch (Exception e) {
            log.error("用户删除异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("用户删除失败，请稍后重试");
        }
    }


    /**
     * 获取用户详细信息（包含角色和权限）
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @Override
    public R<UserDTO> getUserWithRolesAndPermissions(Long userId) {
        try {
            Optional<User> optionalUser = userRepository.findByIdWithRolesAndPermissions(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            UserDTO userDTO = mapperManager.convertToUserDTOWithRolesAndPermissions(optionalUser.get());
            return R.ok(userDTO);

        } catch (Exception e) {
            log.error("获取用户详细信息异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("获取用户信息失败");
        }
    }
}

package hbnu.project.zhiyanauthservice.service.impl;

import hbnu.project.zhiyanauthservice.model.dto.TokenDTO;
import hbnu.project.zhiyanauthservice.model.dto.UserDTO;
import hbnu.project.zhiyanauthservice.model.entity.Role;
import hbnu.project.zhiyanauthservice.model.entity.User;
import hbnu.project.zhiyanauthservice.model.entity.UserRole;
import hbnu.project.zhiyanauthservice.model.enums.SystemRole;
import hbnu.project.zhiyanauthservice.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauthservice.model.form.LoginBody;
import hbnu.project.zhiyanauthservice.model.form.RegisterBody;
import hbnu.project.zhiyanauthservice.model.form.ResetPasswordBody;
import hbnu.project.zhiyanauthservice.model.form.UserProfileUpdateBody;
import hbnu.project.zhiyanauthservice.repository.RoleRepository;
import hbnu.project.zhiyanauthservice.repository.UserRepository;
import hbnu.project.zhiyanauthservice.repository.UserRoleRepository;
import hbnu.project.zhiyanauthservice.service.AuthService;
import hbnu.project.zhiyanauthservice.service.PermissionService;
import hbnu.project.zhiyanauthservice.service.UserService;
import hbnu.project.zhiyanauthservice.service.VerificationCodeService;
import hbnu.project.zhiyancommon.constants.GeneralConstants;
import hbnu.project.zhiyansecurity.utils.SecurityUtils;
import hbnu.project.zhiyansecurity.mapper.SecurityMapperManager;
import hbnu.project.zhiyancommon.domain.R;
import hbnu.project.zhiyancommon.utils.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


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
    private final SecurityMapperManager mapperManager;


    /**
     * 用户注册
     * 流程：验证码校验 -> 邮箱重复检查 -> 密码确认校验 -> 密码加密 -> 创建用户 -> 分配默认角色
     *
     * @param registerBody 注册表单数据
     * @return 注册结果
     */
    @Override
    @Transactional
    public R<UserDTO> register(RegisterBody registerBody) {
        try {
            // 1. 验证码校验
            R<Boolean> codeValidation = verificationCodeService.validateCode(
                    registerBody.getEmail(),
                    registerBody.getVerificationCode(),
                    VerificationCodeType.REGISTER
            );

            if (!codeValidation.getData() || !GeneralConstants.SUCCESS.equals(codeValidation.getCode())) {
                log.warn("用户注册失败 - 验证码校验失败: {}", registerBody.getEmail());
                return R.fail("验证码验证失败");
            }

            // 2. 检查邮箱是否已存在
            if (userRepository.existsByEmail(registerBody.getEmail())) {
                log.warn("用户注册失败 - 邮箱已存在: {}", registerBody.getEmail());
                return R.fail("该邮箱已被注册");
            }

            // 3. 密码确认校验
            if (!registerBody.getPassword().equals(registerBody.getConfirmPassword())) {
                return R.fail("两次输入的密码不一致");
            }

            // 4. 创建用户实体
            User user = User.builder()
                    .email(registerBody.getEmail())
                    .name(registerBody.getName())
                    .passwordHash(SecurityUtils.encryptPassword(registerBody.getPassword()))
                    .title(registerBody.getTitle())
                    .institution(registerBody.getInstitution())
                    .isLocked(false)
                    .isDeleted(false)
                    .build();

            // 5. 保存用户
            user = userRepository.save(user);

            // 6. 为新用户分配默认角色（普通用户）
            assignDefaultRole(user.getId());

            // 7. 构建返回的用户DTO
            UserDTO userDTO = convertToUserDTO(user);

            log.info("用户注册成功 - 邮箱: {}, 用户ID: {}", registerBody.getEmail(), user.getId());
            return R.ok(userDTO, "注册成功");

        } catch (Exception e) {
            log.error("用户注册异常 - 邮箱: {}, 错误: {}", registerBody.getEmail(), e.getMessage(), e);
            return R.fail("注册失败，请稍后重试");
        }
    }


    /**
     * 用户登录
     * 流程：用户验证 -> 密码校验 -> 账号状态检查 -> 生成Token -> 记录登录
     *
     * @param loginBody 登录表单数据
     * @return 登录结果，包含token信息
     */
    @Override
    public R<TokenDTO> login(LoginBody loginBody) {
        try {
            // 1. 根据邮箱查找用户
            Optional<User> optionalUser = userRepository.findByEmailAndIsDeletedFalse(loginBody.getEmail());
            if (optionalUser.isEmpty()) {
                log.warn("用户登录失败 - 用户不存在: {}", loginBody.getEmail());
                return R.fail("用户名或密码错误");
            }

            User user = optionalUser.get();

            // 2. 密码校验
            if (!SecurityUtils.matchesPassword(loginBody.getPassword(), user.getPasswordHash())) {
                log.warn("用户登录失败 - 密码错误: {}", loginBody.getEmail());
                return R.fail("用户名或密码错误");
            }

            // 3. 账号状态检查
            if (user.getIsLocked()) {
                log.warn("用户登录失败 - 账号被锁定: {}", loginBody.getEmail());
                return R.fail("账号已被锁定，请联系管理员");
            }

            // 4. 生成Token
            TokenDTO tokenDTO = authService.generateTokens(user.getId(), loginBody.getRememberMe());

            // 5. 设置用户信息到Token中
            UserDTO userDTO = convertToUserDTO(user);
            tokenDTO.setUser(userDTO);

            log.info("用户登录成功 - 邮箱: {}, 用户ID: {}", loginBody.getEmail(), user.getId());
            return R.ok(tokenDTO, "登录成功");

        } catch (Exception e) {
            log.error("用户登录异常 - 邮箱: {}, 错误: {}", loginBody.getEmail(), e.getMessage(), e);
            return R.fail("登录失败，请稍后重试");
        }
    }


    /**
     * 刷新token
     *
     * @param refreshToken 刷新token
     * @return 新的token信息
     */
    @Override
    public R<TokenDTO> refreshToken(String refreshToken) {
        try {
            // 验证refreshToken并获取用户ID
            Long userId = authService.validateToken(refreshToken);
            if (userId == null) {
                return R.fail("刷新令牌无效或已过期");
            }

            // 检查用户是否存在且未被删除
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();
            if (user.getIsLocked()) {
                return R.fail("账号已被锁定");
            }

            // 生成新的Token
            TokenDTO tokenDTO = authService.generateTokens(userId, true);

            // 设置用户信息
            UserDTO userDTO = convertToUserDTO(user);
            tokenDTO.setUser(userDTO);

            log.info("Token刷新成功 - 用户ID: {}", userId);
            return R.ok(tokenDTO, "Token刷新成功");

        } catch (Exception e) {
            log.error("Token刷新异常 - 错误: {}", e.getMessage(), e);
            return R.fail("Token刷新失败");
        }
    }


    /**
     * 用户登出
     *
     * @param userId 用户ID
     * @param token 当前token
     * @return 登出结果
     */
    @Override
    public R<Void> logout(Long userId, String token) {
        try {
            // 将token加入黑名单
            authService.blacklistToken(token, userId);

            log.info("用户登出成功 - 用户ID: {}", userId);
            return R.ok(null, "登出成功");

        } catch (Exception e) {
            log.error("用户登出异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("登出失败");
        }
    }


    /**
     * 重置密码
     *
     * @param resetPasswordBody 重置密码表单
     * @return 重置结果
     */
    @Override
    @Transactional
    public R<Void> resetPassword(ResetPasswordBody resetPasswordBody) {
        try {
            // 1. 验证码校验
            R<Boolean> codeValidation = verificationCodeService.validateCode(
                    resetPasswordBody.getEmail(),
                    resetPasswordBody.getVerificationCode(),
                    VerificationCodeType.RESET_PASSWORD
            );

            if (!codeValidation.getData() || !GeneralConstants.SUCCESS.equals(codeValidation.getCode())) {
                log.warn("密码重置失败 - 验证码校验失败: {}", resetPasswordBody.getEmail());
                return R.fail("验证码验证失败");
            }

            // 2. 密码确认校验
            if (!resetPasswordBody.getNewPassword().equals(resetPasswordBody.getConfirmPassword())) {
                return R.fail("两次输入的密码不一致");
            }

            // 3. 查找用户
            Optional<User> optionalUser = userRepository.findByEmailAndIsDeletedFalse(resetPasswordBody.getEmail());
            if (optionalUser.isEmpty()) {
                log.warn("密码重置失败 - 用户不存在: {}", resetPasswordBody.getEmail());
                return R.fail("用户不存在");
            }

            // 4. 更新密码
            User user = optionalUser.get();
            user.setPasswordHash(SecurityUtils.encryptPassword(resetPasswordBody.getNewPassword()));
            userRepository.save(user);

            log.info("密码重置成功 - 邮箱: {}", resetPasswordBody.getEmail());
            return R.ok(null, "密码重置成功");

        } catch (Exception e) {
            log.error("密码重置异常 - 邮箱: {}, 错误: {}", resetPasswordBody.getEmail(), e.getMessage(), e);
            return R.fail("密码重置失败，请稍后重试");
        }
    }


    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @Override
    @Transactional
    public R<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            // 1. 查找用户
            Optional<User> optionalUser = userRepository.findByIdAndIsDeletedFalse(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }

            User user = optionalUser.get();

            // 2. 验证旧密码
            if (!SecurityUtils.matchesPassword(oldPassword, user.getPasswordHash())) {
                log.warn("修改密码失败 - 旧密码错误: 用户ID {}", userId);
                return R.fail("原密码错误");
            }

            // 3. 更新密码
            user.setPasswordHash(SecurityUtils.encryptPassword(newPassword));
            userRepository.save(user);

            log.info("密码修改成功 - 用户ID: {}", userId);
            return R.ok(null, "密码修改成功");

        } catch (Exception e) {
            log.error("密码修改异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("密码修改失败，请稍后重试");
        }
    }


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

            UserDTO userDTO = convertToUserDTOWithRolesAndPermissions(optionalUser.get());
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
            if (StringUtils.isNotBlank(updateBody.getName())) {
                user.setName(updateBody.getName());
            }
            if (StringUtils.isNotBlank(updateBody.getTitle())) {
                user.setTitle(updateBody.getTitle());
            }
            if (StringUtils.isNotBlank(updateBody.getInstitution())) {
                user.setInstitution(updateBody.getInstitution());
            }
            if (StringUtils.isNotBlank(updateBody.getAvatarUrl())) {
                user.setAvatarUrl(updateBody.getAvatarUrl());
            }

            user = userRepository.save(user);
            UserDTO userDTO = convertToUserDTO(user);

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

            List<UserDTO> userDTOs = userPage.getContent().stream()
                    .map(this::convertToUserDTO)
                    .collect(Collectors.toList());

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

            UserDTO userDTO = convertToUserDTOWithRolesAndPermissions(optionalUser.get());
            return R.ok(userDTO);

        } catch (Exception e) {
            log.error("获取用户详细信息异常 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("获取用户信息失败");
        }
    }


    /**
     * 为新用户分配默认角色
     *
     * @param userId 用户ID
     */
    private void assignDefaultRole(Long userId) {
        try {
            // 查找默认角色（普通用户）
            Optional<Role> defaultRole = roleRepository.findByName(SystemRole.USER.getRoleName());
            if (defaultRole.isPresent()) {
                UserRole userRole = UserRole.builder()
                        .user(User.builder().id(userId).build())
                        .role(defaultRole.get())
                        .assignedAt(LocalDateTime.now())
                        .build();

                userRoleRepository.save(userRole);
                log.info("为用户分配默认角色成功 - 用户ID: {}", userId);
            } else {
                log.warn("默认角色不存在，无法为用户分配角色 - 用户ID: {}", userId);
            }
        } catch (Exception e) {
            log.error("分配默认角色失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }


    /**
     * 将User实体转换为UserDTO
     *
     * @param user 用户实体
     * @return UserDTO
     */
    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }


    /**
     * 将User实体转换为包含角色和权限的UserDTO
     *
     * @param user 用户实体（包含角色和权限）
     * @return UserDTO
     */
    private UserDTO convertToUserDTOWithRolesAndPermissions(User user) {
        UserDTO userDTO = convertToUserDTO(user);

        // 获取角色列表
        if (user.getUserRoles() != null) {
            List<String> roles = user.getUserRoles().stream()
                    .map(ur -> ur.getRole().getName())
                    .collect(Collectors.toList());
            userDTO.setRoles(roles);

            // 获取权限列表
            Set<String> permissions = user.getUserRoles().stream()
                    .flatMap(ur -> ur.getRole().getRolePermissions().stream())
                    .map(rp -> rp.getPermission().getName())
                    .collect(Collectors.toSet());
            userDTO.setPermissions(new ArrayList<>(permissions));
        }

        return userDTO;
    }
}

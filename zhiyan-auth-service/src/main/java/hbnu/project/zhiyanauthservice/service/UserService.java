package hbnu.project.zhiyanauthservice.service;

import hbnu.project.zhiyanauthservice.model.dto.BatchUserOperationDTO;
import hbnu.project.zhiyanauthservice.model.dto.TokenDTO;
import hbnu.project.zhiyanauthservice.model.dto.UserDTO;
import hbnu.project.zhiyanauthservice.model.dto.UserUpdateDTO;
import hbnu.project.zhiyanauthservice.model.entity.User;
import hbnu.project.zhiyanauthservice.model.form.LoginBody;
import hbnu.project.zhiyanauthservice.model.form.RegisterBody;
import hbnu.project.zhiyanauthservice.model.form.ResetPasswordBody;
import hbnu.project.zhiyanauthservice.model.form.UserProfileUpdateBody;
import hbnu.project.zhiyancommon.domain.R;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 提供用户管理、认证、权限等核心功能
 *
 * @author ErgouTree
 */
public interface UserService {
    /**
     * 用户注册
     *
     * @param registerBody 注册表单数据
     * @return 注册结果
     */
    R<UserDTO> register(RegisterBody registerBody);

    /**
     * 用户登录
     *
     * @param loginBody 登录表单数据
     * @return 登录结果，包含token信息
     */
    R<TokenDTO> login(LoginBody loginBody);

    /**
     * 刷新token
     *
     * @param refreshToken 刷新token
     * @return 新的token信息
     */
    R<TokenDTO> refreshToken(String refreshToken);

    /**
     * 用户登出
     *
     * @param userId 用户ID
     * @param token 当前token
     * @return 登出结果
     */
    R<Void> logout(Long userId, String token);

    /**
     * 重置密码
     *
     * @param resetPasswordBody 重置密码表单
     * @return 重置结果
     */
    R<Void> resetPassword(ResetPasswordBody resetPasswordBody);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    R<Void> changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    R<UserDTO> getCurrentUser(Long userId);

    /**
     * 更新用户资料
     *
     * @param userId 用户ID
     * @param updateBody 更新表单
     * @return 更新结果
     */
    R<UserDTO> updateUserProfile(Long userId, UserProfileUpdateBody updateBody);

    /**
     * 分页查询用户列表（管理员功能）
     *
     * @param pageable 分页参数
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    R<Page<UserDTO>> getUserList(Pageable pageable, String keyword);

    /**
     * 锁定/解锁用户
     *
     * @param userId 用户ID
     * @param isLocked 是否锁定
     * @return 操作结果
     */
    R<Void> lockUser(Long userId, boolean isLocked);

    /**
     * 软删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    R<Void> deleteUser(Long userId);

    /**
     * 获取用户详细信息（包含角色和权限）
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    R<UserDTO> getUserWithRolesAndPermissions(Long userId);
}

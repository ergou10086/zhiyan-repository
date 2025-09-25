package hbnu.project.zhiyanauthservice.repository;

import hbnu.project.zhiyanauthservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 *
 * @author ErgouTree
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据邮箱查找用户
     *
     * @param email 用户邮箱
     * @return 用户对象（可能为空）
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 用户邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查用户名是否已存在
     */
    boolean existsByName(String name);

    /**
     * 根据邮箱查找未删除的用户
     *
     * @param email 用户邮箱
     * @return 用户对象（可能为空）
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * 根据ID查找未删除的用户
     *
     * @param id 用户ID
     * @return 用户对象（可能为空）
     */
    Optional<User> findByIdAndIsDeletedFalse(Long id);

    /**
     * 查询用户及其角色和权限
     *
     * @param userId 用户ID
     * @return 用户对象（可能为空）
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.userRoles ur " +
           "LEFT JOIN FETCH ur.role r " +
           "LEFT JOIN FETCH r.rolePermissions rp " +
           "LEFT JOIN FETCH rp.permission p " +
           "WHERE u.id = :userId AND u.isDeleted = false")
    Optional<User> findByIdWithRolesAndPermissions(@Param("userId") Long userId);
}
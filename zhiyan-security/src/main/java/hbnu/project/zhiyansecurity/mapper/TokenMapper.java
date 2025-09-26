package hbnu.project.zhiyansecurity.mapper;

import hbnu.project.zhiyanauthservice.model.dto.TokenDTO;
import hbnu.project.zhiyanauthservice.model.dto.UserDTO;
import hbnu.project.zhiyansecurity.context.LoginUserBody;
import hbnu.project.zhiyancommon.constants.TokenConstants;
import hbnu.project.zhiyancommon.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Token相关实体转换器
 * 提供Token相关对象之间的可靠转换
 *
 * @author ErgouTree
 */
@Component
public class TokenMapper {

    @Autowired
    private UserMapper userMapper;

    /**
     * 创建TokenDTO对象
     *
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param expiresIn 过期时间（秒）
     * @param userDTO 用户信息
     * @return TokenDTO对象
     */
    public TokenDTO createTokenDTO(String accessToken, String refreshToken, Long expiresIn, UserDTO userDTO) {
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }

        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TokenConstants.TOKEN_TYPE_BEARER)
                .expiresIn(expiresIn)
                .user(userDTO)
                .build();
    }

    /**
     * 创建TokenDTO对象（使用LoginUserBody）
     *
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param expiresIn 过期时间（秒）
     * @param loginUserBody 用户登录信息
     * @return TokenDTO对象
     */
    public TokenDTO createTokenDTO(String accessToken, String refreshToken, Long expiresIn, LoginUserBody loginUserBody) {
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }

        UserDTO userDTO = userMapper.toUserDTO(loginUserBody);
        return createTokenDTO(accessToken, refreshToken, expiresIn, userDTO);
    }

    /**
     * 创建简化的TokenDTO（仅包含访问令牌）
     *
     * @param accessToken 访问令牌
     * @param expiresIn 过期时间（秒）
     * @return 简化的TokenDTO对象
     */
    public TokenDTO createSimpleTokenDTO(String accessToken, Long expiresIn) {
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }

        return TokenDTO.builder()
                .accessToken(accessToken)
                .tokenType(TokenConstants.TOKEN_TYPE_BEARER)
                .expiresIn(expiresIn)
                .build();
    }

    /**
     * 更新TokenDTO中的用户信息
     *
     * @param tokenDTO 待更新的TokenDTO
     * @param userDTO 新的用户信息
     * @return 更新后的TokenDTO
     */
    public TokenDTO updateUserInfo(TokenDTO tokenDTO, UserDTO userDTO) {
        if (tokenDTO == null) {
            return null;
        }

        return TokenDTO.builder()
                .accessToken(tokenDTO.getAccessToken())
                .refreshToken(tokenDTO.getRefreshToken())
                .tokenType(tokenDTO.getTokenType())
                .expiresIn(tokenDTO.getExpiresIn())
                .user(userDTO)
                .build();
    }

    /**
     * 验证TokenDTO的必要字段
     *
     * @param tokenDTO 待验证的TokenDTO
     * @return 验证结果，true表示通过
     */
    public boolean validateTokenDTO(TokenDTO tokenDTO) {
        if (tokenDTO == null) {
            return false;
        }

        // 访问令牌不能为空
        if (StringUtils.isBlank(tokenDTO.getAccessToken())) {
            return false;
        }

        // Token类型检查
        if (StringUtils.isBlank(tokenDTO.getTokenType())) {
            return false;
        }

        // 过期时间检查
        if (tokenDTO.getExpiresIn() == null || tokenDTO.getExpiresIn() <= 0) {
            return false;
        }

        return true;
    }

    /**
     * 从TokenDTO提取用户信息
     *
     * @param tokenDTO TokenDTO对象
     * @return 用户信息，如果不存在则返回null
     */
    public UserDTO extractUserInfo(TokenDTO tokenDTO) {
        if (tokenDTO == null) {
            return null;
        }
        return tokenDTO.getUser();
    }

    /**
     * 从TokenDTO提取LoginUserBody
     *
     * @param tokenDTO TokenDTO对象
     * @return LoginUserBody对象，如果不存在则返回null
     */
    public LoginUserBody extractLoginUserBody(TokenDTO tokenDTO) {
        if (tokenDTO == null || tokenDTO.getUser() == null) {
            return null;
        }
        return userMapper.toLoginUserBody(tokenDTO.getUser());
    }

    /**
     * 创建刷新令牌响应的TokenDTO
     *
     * @param newAccessToken 新的访问令牌
     * @param newRefreshToken 新的刷新令牌（可选）
     * @param expiresIn 过期时间（秒）
     * @param originalTokenDTO 原始TokenDTO（用于保留用户信息）
     * @return 刷新后的TokenDTO对象
     */
    public TokenDTO createRefreshTokenDTO(String newAccessToken, String newRefreshToken, 
                                         Long expiresIn, TokenDTO originalTokenDTO) {
        if (StringUtils.isBlank(newAccessToken)) {
            return null;
        }

        return TokenDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(StringUtils.isNotBlank(newRefreshToken) ? 
                    newRefreshToken : (originalTokenDTO != null ? originalTokenDTO.getRefreshToken() : null))
                .tokenType(TokenConstants.TOKEN_TYPE_BEARER)
                .expiresIn(expiresIn)
                .user(originalTokenDTO != null ? originalTokenDTO.getUser() : null)
                .build();
    }

    /**
     * 检查Token是否包含完整的用户信息
     *
     * @param tokenDTO TokenDTO对象
     * @return 是否包含完整用户信息
     */
    public boolean hasCompleteUserInfo(TokenDTO tokenDTO) {
        if (tokenDTO == null || tokenDTO.getUser() == null) {
            return false;
        }

        UserDTO user = tokenDTO.getUser();
        return user.getId() != null && 
               StringUtils.isNotBlank(user.getEmail()) && 
               StringUtils.isNotBlank(user.getName());
    }

    /**
     * 清除TokenDTO中的敏感信息（保留基本Token信息）
     *
     * @param tokenDTO 待处理的TokenDTO
     * @return 清除敏感信息后的TokenDTO
     */
    public TokenDTO sanitizeTokenDTO(TokenDTO tokenDTO) {
        if (tokenDTO == null) {
            return null;
        }

        return TokenDTO.builder()
                .accessToken(tokenDTO.getAccessToken())
                .tokenType(tokenDTO.getTokenType())
                .expiresIn(tokenDTO.getExpiresIn())
                // 不包含刷新令牌和详细用户信息
                .build();
    }

    /**
     * 比较两个TokenDTO是否表示同一个用户的令牌
     *
     * @param token1 第一个TokenDTO
     * @param token2 第二个TokenDTO
     * @return 是否为同一用户的令牌
     */
    public boolean isSameUserToken(TokenDTO token1, TokenDTO token2) {
        if (token1 == null || token2 == null) {
            return false;
        }

        UserDTO user1 = token1.getUser();
        UserDTO user2 = token2.getUser();

        if (user1 == null || user2 == null) {
            return false;
        }

        return user1.getId() != null && 
               user1.getId().equals(user2.getId());
    }
}

package hbnu.project.zhiyanauthservice.service.impl;

import hbnu.project.zhiyanauthservice.model.dto.TokenDTO;
import hbnu.project.zhiyanauthservice.model.entity.User;
import hbnu.project.zhiyanauthservice.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauthservice.model.form.ChangeEmailBody;
import hbnu.project.zhiyanauthservice.model.form.VerificationCodeBody;
import hbnu.project.zhiyanauthservice.repository.UserRepository;
import hbnu.project.zhiyanauthservice.service.AuthService;
import hbnu.project.zhiyanauthservice.service.VerificationCodeService;
import hbnu.project.zhiyancommon.constants.CacheConstants;
import hbnu.project.zhiyancommon.constants.TokenConstants;
import hbnu.project.zhiyancommon.domain.R;
import hbnu.project.zhiyancommon.service.RedisService;
import hbnu.project.zhiyancommon.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 处理JWT令牌、验证码等认证相关核心逻辑
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final VerificationCodeService verificationCodeService;
    private final UserRepository userRepository;
    private final RedisService redisService;
    private final JwtUtils jwtUtils;


    /**
     * 发送验证码
     */
    @Override
    public R<Void> sendVerificationCode(VerificationCodeBody verificationCodeBody) {
        try {
            VerificationCodeType type = VerificationCodeType.valueOf(verificationCodeBody.getType().toUpperCase());
            return verificationCodeService.generateAndSendCode(verificationCodeBody.getEmail(), type);
        } catch (Exception e) {
            log.error("发送验证码失败 - 邮箱: {}, 类型: {}, 错误: {}", 
                verificationCodeBody.getEmail(), verificationCodeBody.getType(), e.getMessage(), e);
            return R.fail("发送验证码失败，请稍后重试");
        }
    }


    /**
     * 验证验证码
     */
    @Override
    public R<Boolean> verifyCode(String email, String code, String type) {
        try {
            VerificationCodeType codeType = VerificationCodeType.valueOf(type.toUpperCase());
            return verificationCodeService.validateCode(email, code, codeType);
        } catch (Exception e) {
            log.error("验证验证码失败 - 邮箱: {}, 类型: {}, 错误: {}", email, type, e.getMessage(), e);
            return R.fail("验证验证码失败，请稍后重试");
        }
    }


    /**
     * 生成JWT令牌
     */
    @Override
    public TokenDTO generateTokens(Long userId, boolean rememberMe) {
        try {
            // 根据记住我选项确定过期时间（分钟）
            int accessTokenExpireMinutes = rememberMe ? 
                TokenConstants.REMEMBER_ME_REFRESH_TOKEN_EXPIRE_MINUTES : TokenConstants.DEFAULT_ACCESS_TOKEN_EXPIRE_MINUTES;
            int refreshTokenExpireMinutes = rememberMe ? 
                TokenConstants.REMEMBER_ME_REFRESH_TOKEN_EXPIRE_MINUTES : TokenConstants.DEFAULT_REFRESH_TOKEN_EXPIRE_MINUTES;
            
            // 生成访问令牌
            String accessToken = jwtUtils.createToken(userId.toString(), accessTokenExpireMinutes);
            
            // 生成刷新令牌（使用更长的过期时间）
            String refreshToken = jwtUtils.createToken(userId.toString(), refreshTokenExpireMinutes);
            
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAccessToken(accessToken);
            tokenDTO.setRefreshToken(refreshToken);
            tokenDTO.setTokenType(TokenConstants.TOKEN_TYPE_BEARER);
            // 转换为秒
            tokenDTO.setExpiresIn((long) accessTokenExpireMinutes * 60);
            
            // 将token存储到Redis中，用于校验和管理（记住我情况下使用更长的缓存时间）
            String tokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
            long cacheTimeSeconds = (long) accessTokenExpireMinutes * 60;
            redisService.setCacheObject(tokenKey, accessToken, cacheTimeSeconds, TimeUnit.SECONDS);
            
            log.info("JWT令牌生成成功 - 用户ID: {}, 记住我: {}", userId, rememberMe);
            return tokenDTO;
            
        } catch (Exception e) {
            log.error("生成JWT令牌失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            throw new RuntimeException("生成令牌失败");
        }
    }


    /**
     * 验证JWT令牌
     */
    @Override
    public Long validateToken(String token) {
        try {
            if (!jwtUtils.validateToken(token)) {
                return null;
            }
            
            String userIdStr = jwtUtils.parseToken(token);
            return Long.parseLong(userIdStr);
            
        } catch (Exception e) {
            log.debug("JWT令牌验证失败 - token: {}, 错误: {}", token, e.getMessage());
            return null;
        }
    }


    /**
     * 将token加入黑名单
     */
    @Override
    public void blacklistToken(String token, Long userId) {
        try {
            // 获取token的剩余有效时间
            Long remainingTime = jwtUtils.getRemainingTime(token);
            if (remainingTime != null && remainingTime > 0) {
                String blacklistKey = CacheConstants.TOKEN_BLACKLIST_PREFIX + token;
                redisService.setCacheObject(blacklistKey, userId.toString(), remainingTime, TimeUnit.SECONDS);
            }
            
            // 同时清除用户的token缓存
            String userTokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
            redisService.deleteObject(userTokenKey);
            
            log.info("Token已加入黑名单 - 用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("加入token黑名单失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }


    /**
     * 检查token是否在黑名单中
     */
    @Override
    public boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = CacheConstants.TOKEN_BLACKLIST_PREFIX + token;
            return redisService.hasKey(blacklistKey);
        } catch (Exception e) {
            log.debug("检查token黑名单状态失败 - token: {}, 错误: {}", token, e.getMessage());
            return false;
        }
    }


    /**
     * 修改邮箱
     */
    @Override
    public R<Void> changeEmail(Long userId, ChangeEmailBody changeEmailBody) {
        try {
            // 1. 验证新邮箱的验证码
            R<Boolean> codeValidation = verificationCodeService.validateCode(
                changeEmailBody.getNewEmail(),
                changeEmailBody.getVerificationCode(),
                VerificationCodeType.CHANGE_EMAIL
            );
            
            if (!codeValidation.getData()) {
                return R.fail("验证码验证失败");
            }
            
            // 2. 检查新邮箱是否已被使用
            if (userRepository.existsByEmail(changeEmailBody.getNewEmail())) {
                return R.fail("该邮箱已被其他用户使用");
            }
            
            // 3. 更新用户邮箱
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return R.fail("用户不存在");
            }
            
            User user = optionalUser.get();
            user.setEmail(changeEmailBody.getNewEmail());
            userRepository.save(user);
            
            log.info("用户邮箱修改成功 - 用户ID: {}, 新邮箱: {}", userId, changeEmailBody.getNewEmail());
            return R.ok(null, "邮箱修改成功");
            
        } catch (Exception e) {
            log.error("修改邮箱失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return R.fail("修改邮箱失败，请稍后重试");
        }
    }
}

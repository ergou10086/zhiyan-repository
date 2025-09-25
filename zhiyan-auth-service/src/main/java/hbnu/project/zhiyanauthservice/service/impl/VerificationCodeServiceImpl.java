package hbnu.project.zhiyanauthservice.service.impl;

import hbnu.project.zhiyanauthservice.model.entity.VerificationCode;
import hbnu.project.zhiyanauthservice.model.enums.VerificationCodeType;
import hbnu.project.zhiyanauthservice.repository.VerificationCodeRepository;
import hbnu.project.zhiyanauthservice.service.VerificationCodeService;
import hbnu.project.zhiyanauthservice.utils.VerificationCodeGenerator;
import hbnu.project.zhiyancommon.domain.R;
import hbnu.project.zhiyancommon.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final RedisService redisService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;

    // Redis键前缀
    private static final String VERIFICATION_CODE_PREFIX = "verification_code:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:verification_code:";
    private static final String USED_CODE_PREFIX = "used_verification_code:";

    // 验证码配置
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 10; // 验证码10分钟过期
    private static final int RATE_LIMIT_MINUTES = 1; // 1分钟内只能发送一次

    @Value("${spring.mail.username:noreply@zhiyan-platform.com}")
    private String fromEmail;

    @Value("${app.name:智研平台}")
    private String appName;

    @Override
    @Transactional
    public R<Void> generateAndSendCode(String email, VerificationCodeType type) {
        try {
            // 检查发送频率限制
            if (!canSendCode(email, type)) {
                return R.fail("验证码发送过于频繁，请稍后再试");
            }

            // 生成验证码
            String code = VerificationCodeGenerator.generateNumericCode(CODE_LENGTH);

            // 存储到Redis（主要用于快速验证）
            String redisKey = buildRedisKey(email, type);
            redisService.setCacheObject(redisKey, code, (long) CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 同时存储到数据库（用于持久化和审计）
            VerificationCode verificationCode = VerificationCode.builder()
                    .email(email)
                    .code(code)
                    .type(type)
                    .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES))
                    .isUsed(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            verificationCodeRepository.save(verificationCode);

            // 发送邮件
            sendCodeByEmail(email, code, type);

            // 设置发送频率限制
            String rateLimitKey = buildRateLimitKey(email, type);
            redisService.setCacheObject(rateLimitKey, "1", (long) RATE_LIMIT_MINUTES, TimeUnit.MINUTES);

            log.info("验证码发送成功 - 邮箱: {}, 类型: {}", email, type);
            return R.ok(null, "验证码发送成功");

        } catch (Exception e) {
            log.error("验证码发送失败 - 邮箱: {}, 类型: {}, 错误: {}", email, type, e.getMessage(), e);
            return R.fail("验证码发送失败，请稍后重试");
        }
    }

    @Override
    public R<Boolean> validateCode(String email, String code, VerificationCodeType type) {
        try {
            // 检查验证码是否已被使用
            String usedKey = buildUsedCodeKey(email, code, type);
            if (redisService.hasKey(usedKey)) {
                log.warn("验证码已被使用 - 邮箱: {}, 验证码: {}, 类型: {}", email, code, type);
                return R.ok(false, "验证码已被使用");
            }

            // 先从Redis验证（快速）
            String redisKey = buildRedisKey(email, type);
            String storedCode = redisService.getCacheObject(redisKey);

            if (storedCode != null && storedCode.equals(code)) {
                // Redis验证成功，标记为已使用
                markCodeAsUsed(email, code, type);
                log.info("验证码验证成功(Redis) - 邮箱: {}, 类型: {}", email, type);
                return R.ok(true, "验证码验证成功");
            }

            // Redis中没有，从数据库验证（兜底）
            var optionalCode = verificationCodeRepository
                    .findByEmailAndCodeAndTypeAndIsUsedFalse(email, code, type);

            if (optionalCode.isPresent()) {
                VerificationCode verificationCode = optionalCode.get();

                // 检查是否过期
                if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
                    log.warn("验证码已过期 - 邮箱: {}, 类型: {}", email, type);
                    return R.ok(false, "验证码已过期");
                }

                // 标记数据库中的验证码为已使用
                verificationCode.setIsUsed(true);
                verificationCodeRepository.save(verificationCode);

                // 同时标记Redis中的验证码为已使用
                markCodeAsUsed(email, code, type);

                log.info("验证码验证成功(数据库) - 邮箱: {}, 类型: {}", email, type);
                return R.ok(true, "验证码验证成功");
            }

            log.warn("验证码验证失败 - 邮箱: {}, 验证码: {}, 类型: {}", email, code, type);
            return R.ok(false, "验证码错误或已过期");

        } catch (Exception e) {
            log.error("验证码验证异常 - 邮箱: {}, 类型: {}, 错误: {}", email, type, e.getMessage(), e);
            return R.fail("验证码验证失败，请稍后重试");
        }
    }

    @Override
    public boolean canSendCode(String email, VerificationCodeType type) {
        String rateLimitKey = buildRateLimitKey(email, type);
        return !redisService.hasKey(rateLimitKey);
    }

    @Override
    @Transactional
    public void cleanExpiredCodes() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = verificationCodeRepository.deleteExpiredCodes(now);
            log.info("清理过期验证码完成，删除数量: {}", deletedCount);
        } catch (Exception e) {
            log.error("清理过期验证码失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void markCodeAsUsed(String email, String code, VerificationCodeType type) {
        // 在Redis中标记为已使用，设置较长的过期时间防止重复使用
        String usedKey = buildUsedCodeKey(email, code, type);
        redisService.setCacheObject(usedKey, "1", (long) CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 删除原始验证码缓存
        String redisKey = buildRedisKey(email, type);
        redisService.deleteObject(redisKey);
    }

    /**
     * 发送验证码邮件
     */
    private void sendCodeByEmail(String email, String code, VerificationCodeType type) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(buildEmailSubject(type));
            message.setText(buildEmailContent(code, type));

            mailSender.send(message);
            log.info("验证码邮件发送成功 - 邮箱: {}", email);

        } catch (Exception e) {
            log.error("验证码邮件发送失败 - 邮箱: {}, 错误: {}", email, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 构建Redis键
     */
    private String buildRedisKey(String email, VerificationCodeType type) {
        return VERIFICATION_CODE_PREFIX + type.name().toLowerCase() + ":" + email;
    }

    /**
     * 构建频率限制键
     */
    private String buildRateLimitKey(String email, VerificationCodeType type) {
        return RATE_LIMIT_PREFIX + type.name().toLowerCase() + ":" + email;
    }

    /**
     * 构建已使用验证码键
     */
    private String buildUsedCodeKey(String email, String code, VerificationCodeType type) {
        return USED_CODE_PREFIX + type.name().toLowerCase() + ":" + email + ":" + code;
    }

    /**
     * 构建邮件主题
     */
    private String buildEmailSubject(VerificationCodeType type) {
        return switch (type) {
            case REGISTER -> appName + " - 注册验证码";
            case RESET_PASSWORD -> appName + " - 密码重置验证码";
            case CHANGE_EMAIL -> appName + " - 邮箱变更验证码";
            case LOGIN_VERIFICATION -> appName + " - 登录验证码";
        };
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code, VerificationCodeType type) {
        String action = switch (type) {
            case REGISTER -> "注册账户";
            case RESET_PASSWORD -> "重置密码";
            case CHANGE_EMAIL -> "变更邮箱";
            case LOGIN_VERIFICATION -> "登录验证";
        };

        return String.format(
                "您好！\n\n" +
                        "您正在进行%s操作，验证码为：%s\n\n" +
                        "验证码有效期为%d分钟，请及时使用。\n" +
                        "如果这不是您的操作，请忽略此邮件。\n\n" +
                        "%s团队",
                action, code, CODE_EXPIRE_MINUTES, appName
        );
    }
}

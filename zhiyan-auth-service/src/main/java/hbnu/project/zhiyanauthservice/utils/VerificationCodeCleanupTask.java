package hbnu.project.zhiyanauthservice.utils;

import hbnu.project.zhiyanauthservice.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 验证码清理定时任务
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.verification-code.enable-cleanup-task", havingValue = "true", matchIfMissing = true)
public class VerificationCodeCleanupTask {

    private final VerificationCodeService verificationCodeService;

    /**
     * 每小时清理一次过期验证码
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredCodes() {
        log.info("开始清理过期验证码");
        try {
            verificationCodeService.cleanExpiredCodes();
            log.info("过期验证码清理完成");
        } catch (Exception e) {
            log.error("过期验证码清理失败", e);
        }
    }
}

package hbnu.project.zhiyancommon.constants;

/**
 * 缓存常量
 *
 * @author ErgouTree
 */
public class CacheConstants {

    /**
     * 验证码相关缓存键前缀
     */
    public static final String VERIFICATION_CODE_PREFIX = "verification_code:";
    public static final String RATE_LIMIT_PREFIX = "rate_limit:verification_code:";
    public static final String USED_CODE_PREFIX = "used_verification_code:";

    /**
     * 认证相关缓存键前缀
     */
    public static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    public static final String USER_TOKEN_PREFIX = "user:token:";
    public static final String USER_SESSION_PREFIX = "user:session:";

    /**
     * 缓存过期时间（秒）
     */
    public static final long VERIFICATION_CODE_EXPIRE = 600L; // 10分钟
    public static final long RATE_LIMIT_EXPIRE = 60L; // 1分钟
    public static final long TOKEN_EXPIRE = 7200L; // 2小时
    public static final long SESSION_EXPIRE = 1800L; // 30分钟

    /**
     * 私有构造函数，防止实例化
     */
    private CacheConstants() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}
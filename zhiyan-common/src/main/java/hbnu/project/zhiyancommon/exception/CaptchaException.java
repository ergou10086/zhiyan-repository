package hbnu.project.zhiyancommon.exception;

/**
 * 验证码错误异常类
 *
 * @author ErgouTree
 */
public class CaptchaException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public CaptchaException(String msg) {
        super(msg);
    }
}

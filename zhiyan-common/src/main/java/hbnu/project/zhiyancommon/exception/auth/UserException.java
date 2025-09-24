package hbnu.project.zhiyancommon.exception.auth;

import hbnu.project.zhiyancommon.exception.base.BaseException;

/**
 * 用户信息异常类
 * 
 * @author ErgouTree
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}

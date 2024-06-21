package com.frank.bi.constant;

/**
 * 用户常量
 *
 * @author Frank
 */
public class UserConstant {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "frank";

    /**
     * 用户登录态键
     */
    public static final String USER_LOGIN_STATE = "user_login";

    /**
     * 默认角色
     */
    public static final String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    public static final String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    public static final String BAN_ROLE = "ban";

    /**
     * 默认头像
     */
    public static final String DEFAULT_AVATAR = "https://avatars.githubusercontent.com/u/48648302?v=4";

    /**
     * 用户名最短值
     */
    public static final int USER_ACCOUNT_MINLENGTH = 4;

    /**
     * 用户密码最短值
     */
    public static final int USER_PASSWORD_MINLENGTH = 8;
}

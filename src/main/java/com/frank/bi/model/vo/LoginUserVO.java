package com.frank.bi.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 已登录用户（脱敏）
 *
 * @author Frank
 */
@Data
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色：user / admin
     */
    private String userRole;

    /**
     * 性别：男 / 女
     */
    private String gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态
     * 0：正常
     * 1：注销
     * 2：封号
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
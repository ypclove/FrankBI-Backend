package com.frank.bi.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户（脱敏）
 *
 * @author Frank
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

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
     * 用户账号
     */
    private String userAccount;

    /**
     * 创建时间
     */
    private Date createTime;
}
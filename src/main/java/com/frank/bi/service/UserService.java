package com.frank.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.frank.bi.model.dto.user.UserAddRequest;
import com.frank.bi.model.dto.user.UserQueryRequest;
import com.frank.bi.model.dto.user.UserRegisterRequest;
import com.frank.bi.model.dto.user.UserUpdateMyRequest;
import com.frank.bi.model.entity.User;
import com.frank.bi.model.vo.LoginUserVO;
import com.frank.bi.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Frank
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求参数
     * @return userID
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      HttpServletRequest
     * @return 已登录用户信息（脱敏）
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销（退出登录）
     *
     * @param request HttpServletRequest
     * @return 用户注销是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request HttpServletRequest
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user 用户信息
     * @return 已登录用户（脱敏）
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 管理员添加用户
     *
     * @param userAddRequest 创建用户请求参数
     * @return userID
     */
    long addUser(UserAddRequest userAddRequest);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户信息
     * @return 用户信息（脱敏）
     */
    UserVO getUserVO(User user);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList 用户列表
     * @return 用户列表（脱敏）
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 用户更新个人信息请求
     * @param request             HttpServletRequest
     * @return 更新个人信息是否成功
     */
    boolean updateMyUser(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request HttpServletRequest
     * @return 当前登录用户
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request HttpServletRequest
     * @return 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user 用户
     * @return 是否为管理员
     */
    boolean isAdmin(User user);
}

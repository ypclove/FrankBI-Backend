package com.frank.bi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.frank.bi.annotation.AuthCheck;
import com.frank.bi.common.BaseResponse;
import com.frank.bi.common.DeleteRequest;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.common.ResultUtils;
import com.frank.bi.constant.UserConstant;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.exception.ThrowUtils;
import com.frank.bi.model.dto.user.*;
import com.frank.bi.model.entity.User;
import com.frank.bi.model.entity.UserCode;
import com.frank.bi.model.vo.LoginUserVO;
import com.frank.bi.model.vo.UserCodeVO;
import com.frank.bi.model.vo.UserVO;
import com.frank.bi.service.UserCodeService;
import com.frank.bi.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户接口
 *
 * @author Frank
 */
@Slf4j
@RestController
@Api(tags = "UserController")
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserCodeService userCodeService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return userID
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          HttpServletRequest
     * @return 已登录用户信息（脱敏）
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销（退出登录）
     *
     * @param request HttpServletRequest
     * @return 用户注销是否成功
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request HttpServletRequest
     * @return 已登录用户信息（脱敏）
     */
    @GetMapping("/get/login")
    @ApiOperation(value = "获取当前登录用户")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 管理员添加用户
     *
     * @param userAddRequest 创建用户请求
     * @return userID
     */
    @PostMapping("/add")
    @ApiOperation(value = "管理员添加用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = userService.addUser(userAddRequest);
        ThrowUtils.throwIf(userId < 0, ErrorCode.PARAMS_ERROR, "添加用户失败");
        return ResultUtils.success(userId);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 删除用户请求
     * @return 删除用户是否成功
     */
    @PostMapping("/delete")
    @ApiOperation(value = "管理员删除用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isDel = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(isDel);
    }

    /**
     * 管理员更新用户
     *
     * @param userUpdateRequest 更新用户请求
     * @return 更新用户是否成功
     */
    @PostMapping("/update")
    @ApiOperation(value = "管理员更新用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户更新信息
     *
     * @param userUpdateRequest 更新用户请求
     * @return 更新用户是否成功
     */
    @PostMapping("/update/user")
    @ApiOperation(value = "更新用户信息")
    public BaseResponse<Boolean> updateByProfileUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id 用户 id
     * @return 用户信息
     */
    @GetMapping("/get")
    @ApiOperation(value = "管理员根据id获取用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id 用户 id
     * @return 用户编号
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取包装类")
    public BaseResponse<UserCodeVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        QueryWrapper<UserCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", id);
        UserCode userCode = userCodeService.getOne(queryWrapper);
        User user = response.getData();
        // 脱敏，将密码设置为空
        user.setUserPassword("");
        UserCodeVO userCodeVO = new UserCodeVO();
        // 将user的属性复制给userCodeVO
        BeanUtils.copyProperties(user, userCodeVO);
        userCodeVO.setId(userCode.getId());
        return ResultUtils.success(userCodeVO);
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest 用户查询请求
     * @return 用户列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "分页获取用户列表（仅管理员）")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest 用户查询请求
     * @return 用户列表（脱敏）
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取用户封装列表")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVoPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVoPage.setRecords(userVO);
        return ResultUtils.success(userVoPage);
    }

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 用户更新个人信息请求
     * @param request             HttpServletRequest
     * @return 更新个人信息是否成功
     */
    @PostMapping("/update/my")
    @ApiOperation(value = "用户更新个人信息")
    public BaseResponse<Boolean> updateMyInfo(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean updateMyUser = userService.updateMyUser(userUpdateMyRequest, request);
        return ResultUtils.success(updateMyUser);
    }
}

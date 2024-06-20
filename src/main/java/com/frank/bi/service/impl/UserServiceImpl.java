package com.frank.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.constant.CommonConstant;
import com.frank.bi.constant.UserConstant;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.exception.ThrowUtils;
import com.frank.bi.mapper.UserMapper;
import com.frank.bi.model.dto.user.UserAddRequest;
import com.frank.bi.model.dto.user.UserQueryRequest;
import com.frank.bi.model.dto.user.UserRegisterRequest;
import com.frank.bi.model.dto.user.UserUpdateMyRequest;
import com.frank.bi.model.entity.AiFrequency;
import com.frank.bi.model.entity.User;
import com.frank.bi.model.entity.UserCode;
import com.frank.bi.model.enums.UserRoleEnum;
import com.frank.bi.model.vo.LoginUserVO;
import com.frank.bi.model.vo.UserVO;
import com.frank.bi.service.AiFrequencyService;
import com.frank.bi.service.UserCodeService;
import com.frank.bi.service.UserService;
import com.frank.bi.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author Frank
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private AiFrequencyService aiFrequencyService;

    @Resource
    private UserCodeService userCodeService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求参数
     * @return userID
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < UserConstant.USER_ACCOUNT_MINLENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名少于 4 个字符");
        }
        if (userPassword.length() < UserConstant.USER_PASSWORD_MINLENGTH || checkPassword.length() < UserConstant.USER_PASSWORD_MINLENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码少于 8 个字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        synchronized (userAccount.intern()) {
            // 账户不能重复
            isCodeAndAccountExist(userAccount);
            // 3. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserName(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserAvatar(UserConstant.DEFAULT_AVATAR);
            // user.setUserCode(userCode);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }

            // 用户注册，保存用户调用次数
            AiFrequency aiFrequency = new AiFrequency();
            aiFrequency.setUserId(user.getId());
            aiFrequencyService.save(aiFrequency);

            // 用户编号自增
            UserCode code = new UserCode();
            code.setUserId(user.getId());
            userCodeService.save(code);

            return user.getId();
        }
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      HttpServletRequest
     * @return 已登录用户信息（脱敏）
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < UserConstant.USER_ACCOUNT_MINLENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < UserConstant.USER_PASSWORD_MINLENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("用户登录失败，账号密码错误");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 用户注销（退出登录）
     *
     * @param request HttpServletRequest
     * @return 用户注销是否成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取当前登录用户
     *
     * @param request HttpServletRequest
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取已登录用户信息（脱敏）
     *
     * @param user 已登录的用户
     * @return 已登录用户信息（脱敏）
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 管理员添加用户
     *
     * @param userAddRequest 创建用户请求参数
     * @return userID
     */
    @Override
    public long addUser(UserAddRequest userAddRequest) {
        String userName = userAddRequest.getUserName();
        String userAccount = userAddRequest.getUserAccount();
        String userAvatar = userAddRequest.getUserAvatar();
        String userPassword = userAddRequest.getUserPassword();
        String userRole = userAddRequest.getUserRole();
        String email = userAddRequest.getEmail();
        String phone = userAddRequest.getPhone();
        String gender = userAddRequest.getGender();
        Integer userStatus = userAddRequest.getUserStatus();

        // 账户和编号不能重复
        isCodeAndAccountExist(userAccount);
        // 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
        User user = new User();
        user.setUserName(userName);
        user.setUserAvatar(userAvatar);
        user.setUserRole(userRole);
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserStatus(userStatus);
        user.setEmail(email);
        user.setGender(gender);
        user.setPhone(phone);
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 用户编号自增
        UserCode code = new UserCode();
        code.setUserId(user.getId());
        userCodeService.save(code);

        return user.getId();
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户信息
     * @return 用户信息（脱敏）
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param userList 用户列表
     * @return 用户列表（脱敏）
     */
    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 用户更新个人信息请求
     * @param request             HttpServletRequest
     * @return 更新个人信息是否成功
     */
    @Override
    public boolean updateMyUser(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {

        User loginUser = this.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 判断账号和编号是否重复
     *
     * @param userAccount 用户账号
     */
    private void isCodeAndAccountExist(String userAccount) {
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request HttpServletRequest
     * @return 当前登录用户
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request HttpServletRequest
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    /**
     * 是否为管理员
     *
     * @param user 用户
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }
}

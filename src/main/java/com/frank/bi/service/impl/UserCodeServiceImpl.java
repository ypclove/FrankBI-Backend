package com.frank.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.exception.ThrowUtils;
import com.frank.bi.mapper.UserCodeMapper;
import com.frank.bi.model.entity.UserCode;
import com.frank.bi.service.UserCodeService;
import org.springframework.stereotype.Service;

/**
 * @author Frank
 */
@Service
public class UserCodeServiceImpl extends ServiceImpl<UserCodeMapper, UserCode> implements UserCodeService {

    /**
     * 查看用户有无调用次数
     *
     * @param userId 用户 ID
     * @return 用户编码
     */
    @Override
    public UserCode getUserCodeByUserId(long userId) {
        if (userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserCode> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        UserCode userCode = this.getOne(wrapper);
        ThrowUtils.throwIf(userCode == null, ErrorCode.NULL_ERROR, "此用户不存在");
        return userCode;
    }
}





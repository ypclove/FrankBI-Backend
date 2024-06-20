package com.frank.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.frank.bi.model.entity.UserCode;

/**
 * @author Frank
 */
public interface UserCodeService extends IService<UserCode> {

    /**
     * 查看用户有无调用次数
     *
     * @param userId 用户 ID
     * @return 用户编码
     */
    UserCode getUserCodeByUserId(long userId);
}

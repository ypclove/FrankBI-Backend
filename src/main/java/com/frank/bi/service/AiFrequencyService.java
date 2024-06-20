package com.frank.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.frank.bi.model.entity.AiFrequency;

/**
 * @author Frank
 */
public interface AiFrequencyService extends IService<AiFrequency> {

    /**
     * 调用智能分析接口次数自动减一
     *
     * @param userId 用户 Id
     * @return 次数自动减一是否成功
     */
    boolean invokeAutoDecrease(long userId);

    /**
     * 查看用户是否有调用次数
     *
     * @param userId 用户 ID
     * @return 用户是否有调用次数
     */
    boolean hasFrequency(long userId);
}

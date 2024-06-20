package com.frank.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.exception.ThrowUtils;
import com.frank.bi.mapper.AiFrequencyMapper;
import com.frank.bi.model.entity.AiFrequency;
import com.frank.bi.service.AiFrequencyService;
import org.springframework.stereotype.Service;

/**
 * @author Frank
 */
@Service
public class AiFrequencyServiceImpl extends ServiceImpl<AiFrequencyMapper, AiFrequency> implements AiFrequencyService {

    /**
     * 调用智能分析接口次数自动减一
     *
     * @param userId 用户 Id
     * @return 次数自动减一是否成功
     */
    @Override
    public synchronized boolean invokeAutoDecrease(long userId) {
        if (userId < 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求错误");
        }
        QueryWrapper<AiFrequency> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        AiFrequency aiFrequency = this.getOne(wrapper);
        ThrowUtils.throwIf(aiFrequency == null, ErrorCode.NULL_ERROR, "此用户Id不存在");

        Integer totalFrequency = aiFrequency.getTotalFrequency();
        Integer remainFrequency = aiFrequency.getRemainFrequency();
        // 总调用次数 +1
        totalFrequency = totalFrequency + 1;
        // 剩余次数 -1
        remainFrequency = remainFrequency - 1;

        if (remainFrequency < 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "剩余调用次数为0");
        }
        aiFrequency.setTotalFrequency(totalFrequency);
        aiFrequency.setRemainFrequency(remainFrequency);
        boolean result = this.updateById(aiFrequency);
        ThrowUtils.throwIf(!result, ErrorCode.NULL_ERROR);
        return true;
    }

    /**
     * 查看用户是否有调用次数
     *
     * @param userId 用户 ID
     * @return 用户是否有调用次数
     */
    @Override
    public boolean hasFrequency(long userId) {
        if (userId < 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求错误");
        }
        QueryWrapper<AiFrequency> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        AiFrequency aiFrequency = this.getOne(wrapper);
        ThrowUtils.throwIf(aiFrequency == null, ErrorCode.NULL_ERROR, "用户id不存在");
        int remainFrequency = aiFrequency.getRemainFrequency();
        if (remainFrequency < 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "调用次数不足");
        }
        return true;
    }
}





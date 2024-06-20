package com.frank.bi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.frank.bi.common.BaseResponse;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.common.ResultUtils;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.model.dto.frequency.FrequencyRequest;
import com.frank.bi.model.entity.AiFrequency;
import com.frank.bi.model.entity.User;
import com.frank.bi.model.vo.AiFrequencyVO;
import com.frank.bi.service.AiFrequencyService;
import com.frank.bi.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Frank
 */
@RestController
@RequestMapping("/aiFrequency")
public class AiFrequencyController {

    @Resource
    private UserService userService;

    @Resource
    private AiFrequencyService aiFrequencyService;

    /**
     * 获取 Ai 调用次数
     *
     * @param request HttpServletRequest
     * @return Ai 调用次数
     */
    @GetMapping("/get")
    public BaseResponse<AiFrequencyVO> getAiFrequency(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<AiFrequency> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        AiFrequency aiFrequency = aiFrequencyService.getOne(queryWrapper);
        if (aiFrequency == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "此用户Id不存在");
        }
        Integer remainFrequency = aiFrequency.getRemainFrequency();

        if (remainFrequency < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足1，请及时充值！");
        }
        AiFrequencyVO aiFrequencyVO = new AiFrequencyVO();
        BeanUtils.copyProperties(aiFrequency, aiFrequencyVO);
        return ResultUtils.success(aiFrequencyVO);
    }

    /**
     * 充值接口
     *
     * @param frequency 调用次数
     * @param request   HttpServletRequest
     * @return 充值订单 Id
     */
    @PostMapping("/frequency")
    public BaseResponse<Long> AiFrequencyRecharge(FrequencyRequest frequency, HttpServletRequest request) {
        int frequency1 = frequency.getFrequency();
        if (frequency1 <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您输入的充值次数错误！");
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<AiFrequency> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", loginUser.getId());
        AiFrequency aiFrequencyServiceOne = aiFrequencyService.getOne(wrapper);
        if (aiFrequencyServiceOne == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "还没有次数记录！");
        }
        Integer leftNum = aiFrequencyServiceOne.getRemainFrequency();
        leftNum = leftNum + frequency1;
        aiFrequencyServiceOne.setRemainFrequency(leftNum);
        boolean isUpdate = aiFrequencyService.updateById(aiFrequencyServiceOne);
        if (!isUpdate) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "充值次数失败");
        }
        return ResultUtils.success(aiFrequencyServiceOne.getId());
    }
}

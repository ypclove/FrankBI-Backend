package com.frank.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.frank.bi.model.dto.order.AiFrequencyOrderQueryRequest;
import com.frank.bi.model.dto.order.AiFrequencyOrderUpdateRequest;
import com.frank.bi.model.entity.AiFrequencyOrder;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Frank
 */
public interface AiFrequencyOrderService extends IService<AiFrequencyOrder> {

    /**
     * 分页获取订单列表
     *
     * @param orderQueryRequest 订单查询请求
     * @return 订单列表
     */
    QueryWrapper<AiFrequencyOrder> getOrderQueryWrapper(AiFrequencyOrderQueryRequest orderQueryRequest);

    /**
     * 修改订单
     *
     * @param orderUpdateRequest 修改订单请求
     * @param request            HttpServletRequest
     * @return 修改订单是否成功
     */
    boolean updateOrderInfo(AiFrequencyOrderUpdateRequest orderUpdateRequest,
                            HttpServletRequest request);
}

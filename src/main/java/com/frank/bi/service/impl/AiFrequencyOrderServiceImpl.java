package com.frank.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.constant.AliPayConstant;
import com.frank.bi.constant.CommonConstant;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.exception.ThrowUtils;
import com.frank.bi.mapper.AiFrequencyOrderMapper;
import com.frank.bi.model.dto.order.AiFrequencyOrderQueryRequest;
import com.frank.bi.model.dto.order.AiFrequencyOrderUpdateRequest;
import com.frank.bi.model.entity.AiFrequencyOrder;
import com.frank.bi.model.entity.User;
import com.frank.bi.model.enums.PayOrderEnum;
import com.frank.bi.service.AiFrequencyOrderService;
import com.frank.bi.service.UserService;
import com.frank.bi.utils.SqlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Frank
 */
@Service
public class AiFrequencyOrderServiceImpl
        extends ServiceImpl<AiFrequencyOrderMapper, AiFrequencyOrder>
        implements AiFrequencyOrderService {

    @Resource
    private UserService userService;

    /**
     * 分页获取订单列表
     *
     * @param orderQueryRequest 订单查询请求
     * @return 订单列表
     */
    @Override
    public QueryWrapper<AiFrequencyOrder> getOrderQueryWrapper(AiFrequencyOrderQueryRequest orderQueryRequest) {
        if (orderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = orderQueryRequest.getId();
        String sortField = orderQueryRequest.getSortField();
        String sortOrder = orderQueryRequest.getSortOrder();

        QueryWrapper<AiFrequencyOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.orderBy(
                SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_DESC),
                sortField);
        return queryWrapper;
    }

    /**
     * 修改订单
     *
     * @param orderUpdateRequest 修改订单请求
     * @param request            HttpServletRequest
     * @return 修改订单是否成功
     */
    @Override
    public boolean updateOrderInfo(AiFrequencyOrderUpdateRequest orderUpdateRequest, HttpServletRequest request) {
        Long purchaseQuantity = orderUpdateRequest.getPurchaseQuantity();
        Long id = orderUpdateRequest.getId();
        User loginUser = userService.getLoginUser(request);
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单不存在");
        }

        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未登录");
        }
        if (purchaseQuantity < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入正确的购买数量");
        }
        AiFrequencyOrder order = new AiFrequencyOrder();
        BeanUtils.copyProperties(orderUpdateRequest, order);
        order.setId(id);
        order.setTotalAmount(purchaseQuantity * AliPayConstant.PRICE);
        order.setOrderStatus(Integer.valueOf(PayOrderEnum.WAIT_PAY.getValue()));
        boolean result = this.updateById(order);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }
}





package com.frank.bi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.frank.bi.annotation.AuthCheck;
import com.frank.bi.common.BaseResponse;
import com.frank.bi.common.DeleteRequest;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.common.ResultUtils;
import com.frank.bi.constant.AliPayConstant;
import com.frank.bi.constant.CommonConstant;
import com.frank.bi.constant.UserConstant;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.exception.ThrowUtils;
import com.frank.bi.model.dto.order.AiFrequencyOrderCancelRequest;
import com.frank.bi.model.dto.order.AiFrequencyOrderQueryRequest;
import com.frank.bi.model.dto.order.AiFrequencyOrderUpdateRequest;
import com.frank.bi.model.entity.AiFrequencyOrder;
import com.frank.bi.model.entity.User;
import com.frank.bi.model.enums.PayOrderEnum;
import com.frank.bi.model.vo.AiFrequencyOrderVO;
import com.frank.bi.ordermq.OrderManageProducer;
import com.frank.bi.service.AiFrequencyOrderService;
import com.frank.bi.service.UserService;
import com.frank.bi.utils.SqlUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单
 *
 * @author Frank
 */
@RestController
@RequestMapping("/order")
public class AiFrequencyOrderController {

    @Resource
    private UserService userService;

    @Resource
    private AiFrequencyOrderService aiFrequencyOrderService;

    @Resource
    private OrderManageProducer orderManageProducer;

    /**
     * 添加订单
     *
     * @param total   充值次数
     * @param request HttpServletRequest
     * @return 添加订单是否成功
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addOrder(long total, HttpServletRequest request) {
        if (total <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "充值次数不能小于0");
        }

        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = loginUser.getId();
        double totalAmount = total * AliPayConstant.PRICE;
        AiFrequencyOrder frequencyOrder = new AiFrequencyOrder();
        frequencyOrder.setUserId(userId);
        frequencyOrder.setPrice(AliPayConstant.PRICE);
        frequencyOrder.setTotalAmount(totalAmount);
        frequencyOrder.setPurchaseQuantity(total);
        boolean save = aiFrequencyOrderService.save(frequencyOrder);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 将订单发送到延迟队列
        orderManageProducer.sendManage(frequencyOrder);
        return ResultUtils.success(true);
    }

    /**
     * 获取订单列表
     *
     * @param request HttpServletRequest
     * @return 订单列表
     */
    @GetMapping("/list")
    public BaseResponse<List<AiFrequencyOrderVO>> getOrderList(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = loginUser.getId();
        QueryWrapper<AiFrequencyOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        List<AiFrequencyOrder> frequencyOrderList = aiFrequencyOrderService.list(wrapper);
        List<AiFrequencyOrderVO> frequencyOrderVOList = new ArrayList<>();
        for (AiFrequencyOrder frequencyOrder : frequencyOrderList) {
            AiFrequencyOrderVO frequencyOrderVO = new AiFrequencyOrderVO();
            BeanUtils.copyProperties(frequencyOrder, frequencyOrderVO);
            frequencyOrderVOList.add(frequencyOrderVO);
        }
        return ResultUtils.success(frequencyOrderVOList);
    }

    /**
     * 分页获取订单列表
     *
     * @param orderQueryRequest 订单查询请求
     * @return 分页订单列表
     */
    @PostMapping("/list/byPage")
    @ApiOperation(value = "（管理员）分页获取订单列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AiFrequencyOrder>> listOrderByPage(@RequestBody AiFrequencyOrderQueryRequest orderQueryRequest) {
        long current = orderQueryRequest.getCurrent();
        long size = orderQueryRequest.getPageSize();
        Page<AiFrequencyOrder> orderPage = aiFrequencyOrderService.page(new Page<>(current, size),
                aiFrequencyOrderService.getOrderQueryWrapper(orderQueryRequest));
        return ResultUtils.success(orderPage);
    }

    /**
     * 分页获取当前用户的订单
     *
     * @param aiFrequencyOrderQueryRequest Ai 订单查询请求
     * @param request                      HttpServletRequest
     * @return 当前用户的订单
     */
    @PostMapping("/my/list/page")
    @ApiOperation(value = "获取个人订单")
    public BaseResponse<Page<AiFrequencyOrder>> listMyOrderByPage(@RequestBody AiFrequencyOrderQueryRequest aiFrequencyOrderQueryRequest,
                                                                  HttpServletRequest request) {
        if (aiFrequencyOrderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        aiFrequencyOrderQueryRequest.setUserId(loginUser.getId());
        long current = aiFrequencyOrderQueryRequest.getCurrent();
        long size = aiFrequencyOrderQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AiFrequencyOrder> chartPage = aiFrequencyOrderService.page(new Page<>(current, size),
                getQueryWrapper(aiFrequencyOrderQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 删除订单
     *
     * @param deleteRequest 删除请求
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除订单")
    public BaseResponse<Boolean> deleteOrder(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isDelete = aiFrequencyOrderService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!isDelete, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(isDelete);
    }

    /**
     * 取消订单
     *
     * @param cancelRequest 取消订单请求
     * @return 取消订单是否成功
     */
    @PostMapping("/cancel")
    @ApiOperation(value = "取消订单")
    public BaseResponse<Boolean> cancelOrder(@RequestBody AiFrequencyOrderCancelRequest cancelRequest) {
        if (cancelRequest == null || cancelRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = cancelRequest.getId();
        Long userId = cancelRequest.getUserId();
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        AiFrequencyOrder order = new AiFrequencyOrder();
        BeanUtils.copyProperties(cancelRequest, order);
        order.setOrderStatus(Integer.valueOf(PayOrderEnum.CANCEL_ORDER.getValue()));
        boolean result = aiFrequencyOrderService.updateById(order);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 修改订单
     *
     * @param orderUpdateRequest 修改订单请求
     * @param request            HttpServletRequest
     * @return 修改订单是否成功
     */
    @PostMapping("/update")
    @ApiOperation(value = "修改订单信息")
    public BaseResponse<Boolean> updateOrder(@RequestBody AiFrequencyOrderUpdateRequest orderUpdateRequest,
                                             HttpServletRequest request) {
        if (orderUpdateRequest == null || orderUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiFrequencyOrder aiFrequencyOrder = new AiFrequencyOrder();
        BeanUtils.copyProperties(orderUpdateRequest, aiFrequencyOrder);
        boolean result = aiFrequencyOrderService.updateOrderInfo(orderUpdateRequest, request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 修改之后进入延迟队列
        orderManageProducer.sendManage(aiFrequencyOrder);
        return ResultUtils.success(true);
    }

    /**
     * 封装查询条件
     *
     * @param aiFrequencyOrderQueryRequest 查询条件
     * @return 查询结果
     */
    private QueryWrapper<AiFrequencyOrder> getQueryWrapper(AiFrequencyOrderQueryRequest aiFrequencyOrderQueryRequest) {
        QueryWrapper<AiFrequencyOrder> queryWrapper = new QueryWrapper<>();
        if (aiFrequencyOrderQueryRequest == null) {
            return queryWrapper;
        }

        Long id = aiFrequencyOrderQueryRequest.getId();
        Long userId = aiFrequencyOrderQueryRequest.getUserId();
        String sortField = aiFrequencyOrderQueryRequest.getSortField();
        String sortOrder = aiFrequencyOrderQueryRequest.getSortOrder();

        // 根据前端传来条件进行拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(
                SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_DESC),
                sortField);
        return queryWrapper;
    }
}

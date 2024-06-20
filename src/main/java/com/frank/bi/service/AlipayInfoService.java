package com.frank.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.frank.bi.model.entity.AlipayInfo;

/**
 * @author Frank
 */
public interface AlipayInfoService extends IService<AlipayInfo> {

    /**
     * 获取支付编号
     *
     * @param orderId 订单 Id
     * @param userId  用户 Id
     * @return 支付编号
     */
    long getPayNo(long orderId, long userId);
}

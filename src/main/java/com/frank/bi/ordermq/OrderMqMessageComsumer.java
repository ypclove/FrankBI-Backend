package com.frank.bi.ordermq;

import com.frank.bi.common.ErrorCode;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.model.entity.AiFrequencyOrder;
import com.frank.bi.service.AiFrequencyOrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 监听订单过期的死信队列
 *
 * @author Frank
 */
@Component
@Slf4j
public class OrderMqMessageComsumer {

    @Resource
    private AiFrequencyOrderService aiFrequencyOrderService;

    /**
     * 死信队列的名称
     */
    public static final String QUEUE_B = "order_delayed_queue";

    /**
     * 监听死信队列
     *
     * @param channel
     * @param dekivery
     */
    @Transactional
    @SneakyThrows
    @RabbitListener(queues = {QUEUE_B}, ackMode = "MANUAL")
    public void receiveMessage(AiFrequencyOrder frequencyOrder, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long dekivery) {
        // 接收到失败的信息
        log.info("订单死信队列={}", frequencyOrder);
        if (frequencyOrder == null) {
            channel.basicNack(dekivery, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        Long id = frequencyOrder.getId();
        AiFrequencyOrder orderServiceById = aiFrequencyOrderService.getById(id);
        Integer status = orderServiceById.getOrderStatus();
        // 查询订单是否已经支付，还没支付，标为过期订单
        if (status != 1) {
            // 更新订单状态
            frequencyOrder.setOrderStatus(2);
            boolean b2 = aiFrequencyOrderService.updateById(frequencyOrder);
            if (!b2) {
                // 拒绝消息
                channel.basicNack(dekivery, false, false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新订单失败");
            }
        }
        // 确认消息
        channel.basicAck(dekivery, false);
    }
}

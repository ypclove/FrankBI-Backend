package com.frank.bi.ordermq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单的延迟队列，下单 10 分钟没付款，订单将过期
 *
 * @author Frank
 */
@Configuration
public class OrderDelayedQueueConfig {

    /**
     * 普通交换机的名称
     */
    public static final String X_EXCHANGE = "order_exchange";

    /**
     * 死信交换机的名称
     */
    public static final String Y_DEAD_LETTER_EXCHANGE = "order_delayed_exchange";

    /**
     * 普通队列的名称
     */
    public static final String QUEUE_A = "order_queue";

    /**
     * 死信队列的名称
     */
    public static final String QUEUE_B = "order_delayed_queue";

    /**
     * 声明 xExchange 别名
     *
     * @return
     */
    @Bean("xExchange")
    public DirectExchange xExchange() {
        return new DirectExchange(X_EXCHANGE);
    }

    /**
     * 声明 yExchange 别名
     *
     * @return
     */
    @Bean("yExchange")
    public DirectExchange yExchange() {
        return new DirectExchange(Y_DEAD_LETTER_EXCHANGE);
    }

    /**
     * 声明普通队列要有 ttl 为 600s
     *
     * @return
     */
    @Bean("queueA")
    public Queue queueA() {
        Map<String, Object> arguments = new HashMap<>(3);
        // 设置死信交换机
        arguments.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        // 设置死信RoutingKey
        arguments.put("x-dead-letter-routing-key", "YD");
        // 设置TTL 600s 单位是ms
        arguments.put("x-message-ttl", 10 * 60 * 1000);
        return QueueBuilder.durable(QUEUE_A).withArguments(arguments).build();
    }

    /**
     * 声明死信队列要有 ttl 为 40s
     *
     * @return
     */
    @Bean("queueD")
    public Queue queueD() {
        return QueueBuilder.durable(QUEUE_B).build();
    }

    /**
     * 声明队列 QA 绑定 X 交换机
     *
     * @param queueA
     * @param xExchange
     * @return
     */
    @Bean
    public Binding queueABindingX(@Qualifier("queueA") Queue queueA,
                                  @Qualifier("xExchange") DirectExchange xExchange) {
        return BindingBuilder.bind(queueA).to(xExchange).with("XA");
    }

    /**
     * 声明队列 QD 绑定 Y 交换机
     *
     * @param queueD
     * @param yExchange
     * @return
     */
    @Bean
    public Binding queueDBindingY(@Qualifier("queueD") Queue queueD,
                                  @Qualifier("yExchange") DirectExchange yExchange) {
        return BindingBuilder.bind(queueD).to(yExchange).with("YD");
    }
}
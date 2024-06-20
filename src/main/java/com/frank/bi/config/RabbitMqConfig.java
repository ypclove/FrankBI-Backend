package com.frank.bi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.frank.bi.constant.MqConstant.*;

/**
 * @author Frank
 */
@Configuration
public class RabbitMqConfig {

    /**
     * BI_ASYNC 声明交换机
     *
     * @return Direct 类型的交换机
     */
    @Bean
    public DirectExchange biAsyncDirectExchange() {
        return new DirectExchange(BI_ASYNC_EXCHANGE, true, false);
    }

    /**
     * BI_ASYNC 声明队列
     *
     * @return Queue 队列
     */
    @Bean
    public Queue biAsyncQueue() {
        return new Queue(BI_ASYNC_QUEUE, true);
    }

    /**
     * BI_ASYNC 绑定交换机和队列
     */
    @Bean
    public Binding biAsyncBindingDirect() {
        return BindingBuilder
                .bind(biAsyncQueue())
                .to(biAsyncDirectExchange())
                .with(BI_ASYNC_ROUTING_KEY);
    }

    /**
     * AI_QUESTION 声明交换机
     *
     * @return Direct 类型的交换机
     */
    @Bean
    public DirectExchange aiQuestionDirectExchange() {
        return new DirectExchange(AI_QUESTION_EXCHANGE, true, false);
    }

    /**
     * AI_QUESTION 声明队列
     *
     * @return Queue 队列
     */
    @Bean
    public Queue aiQuestionQueue() {
        return new Queue(AI_QUESTION_QUEUE, true);
    }

    /**
     * AI_QUESTION 绑定交换机和队列
     */
    @Bean
    public Binding aiQuestionBindingDirect() {
        return BindingBuilder
                .bind(aiQuestionQueue())
                .to(aiQuestionDirectExchange())
                .with(AI_QUESTION_ROUTING_KEY);
    }
}

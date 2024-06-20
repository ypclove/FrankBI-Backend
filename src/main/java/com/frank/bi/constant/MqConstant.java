package com.frank.bi.constant;

/**
 * 应用到 BI 项目当中的 MQ 常量
 *
 * @author Frank
 */
public interface MqConstant {

    /**
     * BI 智能分析消息队列
     */
    String BI_ASYNC_EXCHANGE = "bi_async_exchange";
    String BI_ASYNC_QUEUE = "bi_async_queue";
    String BI_ASYNC_ROUTING_KEY = "bi_async_routing_key";

    /**
     * AI 问答消息队列
     */
    String AI_QUESTION_EXCHANGE = "ai_question_exchange";
    String AI_QUESTION_QUEUE = "ai_question_queue";
    String AI_QUESTION_ROUTING_KEY = "ai_question_routingKey";
}

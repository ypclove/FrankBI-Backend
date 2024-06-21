package com.frank.bi.constant;

/**
 * 应用到 BI 项目当中的 MQ 常量
 *
 * @author Frank
 */
public class MqConstant {

    /**
     * BI 智能分析消息队列
     */
    public static final String BI_ASYNC_EXCHANGE = "bi_async_exchange";
    public static final String BI_ASYNC_QUEUE = "bi_async_queue";
    public static final String BI_ASYNC_ROUTING_KEY = "bi_async_routing_key";

    /**
     * AI 问答消息队列
     */
    public static final String AI_QUESTION_EXCHANGE = "ai_question_exchange";
    public static final String AI_QUESTION_QUEUE = "ai_question_queue";
    public static final String AI_QUESTION_ROUTING_KEY = "ai_question_routingKey";
}

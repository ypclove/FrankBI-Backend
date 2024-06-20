package com.frank.bi.listener;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.exception.ThrowUtils;
import com.frank.bi.manager.AiManager;
import com.frank.bi.model.entity.AiAssistant;
import com.frank.bi.model.enums.AiAssistantStatusEnum;
import com.frank.bi.service.AiAssistantService;
import com.frank.bi.service.ChartService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.frank.bi.constant.MqConstant.AI_QUESTION_QUEUE;

/**
 * @author Frank
 * @date 2024/6/19
 */
@Component
public class BiListener {

    @Resource
    private AiManager aiManager;

    @Resource
    private ChartService chartService;

    @Resource
    private AiAssistantService aiAssistantService;

    /**
     * AI 生成图表（异步消息队列）消费者
     *
     * @param msg 消息
     */
    // @RabbitListener(queues = BI_ASYNC_QUEUE)
    // public void BiAsyncChartListener(String msg) {
    //     JSONObject jsonObject = JSONUtil.parseObj(msg);
    //     String goal = jsonObject.getStr("goal");
    //     System.out.println(goal);
    //     String chartType = jsonObject.getStr("chartType");
    //     String originalData = jsonObject.getStr("chartData");
    //     Long chartId = jsonObject.get("id", Long.class);
    //
    //     // 构造用户输入
    //     StringBuilder userInput = new StringBuilder();
    //     userInput.append("分析需求：").append("\n");
    //     // 拼接分析目标
    //     String userGoal = goal;
    //     if (StringUtils.isNotBlank(chartType)) {
    //         userGoal += "，请使用" + chartType;
    //     }
    //     userInput.append(userGoal).append("\n");
    //     userInput.append("原始数据：").append("\n");
    //     userInput.append(originalData);
    //
    //     // 调用 AI
    //     String chartResult = aiManager.doChat(userInput.toString());
    //
    //     // 解析内容
    //     String[] splits = chartResult.split(GEN_CONTENT_SPLITS);
    //     if (splits.length < GEN_ITEM_NUM) {
    //         throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
    //     }
    //     // 首次生成的图表数据内容
    //     String preGenChart = splits[GEN_CHART_IDX].trim();
    //     // 图表结论内容
    //     String genResult = splits[GEN_RESULT_IDX].trim();
    //     // 图表结构和数据内容
    //     String validGenChart = ChartUtils.getValidGenChart(preGenChart);
    //
    //     // 将 Ai 生成的分析结论和插入数据到数据库
    //     Chart chart = chartService.getById(chartId);
    //     chart.setGenChart(preGenChart);
    //     chart.setGenResult(genResult);
    //     chart.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
    //     boolean saveResult = chartService.updateById(chart);
    //     ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
    //     System.out.println("消息成功被消费");
    // }

    /**
     * AI 问答（异步消息队列）消费者
     *
     * @param msg 消息
     */
    @RabbitListener(queues = AI_QUESTION_QUEUE)
    public void AiQuestionListener(String msg) {
        JSONObject jsonObject = JSONUtil.parseObj(msg);
        String questionGoal = jsonObject.getStr("questionGoal");
        Long questionId = jsonObject.get("id", Long.class);

        String result = aiManager.doChat(questionGoal);

        // 更新数据
        AiAssistant aiAssistant = aiAssistantService.getById(questionId);
        aiAssistant.setQuestionResult(result);
        aiAssistant.setQuestionStatus(AiAssistantStatusEnum.SUCCEED.getValue());
        boolean saveResult = aiAssistantService.updateById(aiAssistant);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
    }

    // TODO: Order
}

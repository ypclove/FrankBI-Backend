package com.frank.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.frank.bi.model.dto.chart.GenChartByAiRequest;
import com.frank.bi.model.entity.Chart;
import com.frank.bi.model.vo.BiResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Frank
 */
public interface ChartService extends IService<Chart> {

    /**
     * AI 生成图表（同步）
     *
     * @param multipartFile       用户上传的文件
     * @param genChartByAiRequest AI 生成图表请求
     * @param request             HttpServletRequest
     * @return BI 返回结果
     */
    BiResponse genChartByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest,
                            HttpServletRequest request);

    /**
     * AI 生成图表（异步线程池）
     *
     * @param multipartFile       用户上传的文件
     * @param genChartByAiRequest AI 生成图表请求
     * @param request             HttpServletRequest
     * @return BI 返回结果
     */
    BiResponse genChartByAiAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest,
                                 HttpServletRequest request);

    /**
     * AI 生成图表（异步消息队列）
     *
     * @param multipartFile       用户上传的文件
     * @param genChartByAiRequest AI 生成图表请求
     * @param request             HttpServletRequest
     * @return BI 返回结果
     */
    BiResponse genChartByAiAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest,
                                   HttpServletRequest request);

    /**
     * 处理图表更新错误
     *
     * @param chartId     图表 Id
     * @param execMessage 执行消息
     */
    void handleChartUpdateError(long chartId, String execMessage);
}

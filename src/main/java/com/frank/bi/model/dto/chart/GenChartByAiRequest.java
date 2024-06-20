package com.frank.bi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 生成图表请求
 *
 * @author Frank
 */
@Data
public class GenChartByAiRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 图表名称
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;
}
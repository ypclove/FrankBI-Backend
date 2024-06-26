package com.frank.bi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建图表请求
 *
 * @author Frank
 */
@Data
public class ChartAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表信息
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;
}
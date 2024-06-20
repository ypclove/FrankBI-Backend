package com.frank.bi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 图表编辑请求
 *
 * @author Frank
 */
@Data
public class ChartEditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 修改对应图表id
     */
    private Long id;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;
}
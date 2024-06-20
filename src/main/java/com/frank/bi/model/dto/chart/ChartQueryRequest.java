package com.frank.bi.model.dto.chart;

import com.frank.bi.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 图表查询请求
 *
 * @author Frank
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChartQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表名称
     */
    private String chartName;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 创建图标用户 id
     */
    private Long userId;
}
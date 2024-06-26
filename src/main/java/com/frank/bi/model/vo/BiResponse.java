package com.frank.bi.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * BI 返回结果
 *
 * @author Frank
 */
@Data
public class BiResponse {

    private String genChart;

    private String genResult;

    /**
     * 新生成的ID
     */
    private Long chartId;

    private Date createTime;

    private String chartType;

    private String goal;

    private String chartData;

    private String chartName;
}

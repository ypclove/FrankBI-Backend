package com.frank.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.frank.bi.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
 * @author Frank
 */
public interface ChartMapper extends BaseMapper<Chart> {

    /**
     * @param querySql
     * @return
     */
    List<Map<String, Object>> queryChartData(String querySql);
}





package com.frank.bi.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author Frank
 */
public class ChartConstant {

    /**
     * AI生成的内容分隔符
     */
    public static final String GEN_CONTENT_SPLITS = "【【【【【";

    /**
     * AI 生成的内容的元素为 3 个
     */
    public static final int GEN_ITEM_NUM = 3;

    /**
     * 生成图表的数据下标
     */
    public static final int GEN_CHART_IDX = 1;

    /**
     * 生成图表的分析结果的下标
     */
    public static final int GEN_RESULT_IDX = 2;

    /**
     * 提取生成的图表的 Echarts 配置的正则
     */
    public static final String GEN_CHART_REGEX = "\\{(?>[^{}]*(?:\\{[^{}]*}[^{}]*)*)}";

    /**
     * 图表默认名称的前缀
     */
    public static final String DEFAULT_CHART_NAME_PREFIX = "分析图表_";

    /**
     * 图表默认名称的后缀长度
     */
    public static final int DEFAULT_CHART_NAME_SUFFIX_LEN = 10;

    /**
     * 图表上传文件大小 2M
     */
    public static final long FILE_MAX_SIZE = 2 * 1024 * 1024L;

    /**
     * 图表上传文件后缀白名单
     */
    public static final List<String> VALID_FILE_SUFFIX = Arrays.asList("xlsx", "csv", "xls", "json");

    /**
     * 用户头像上传文件大小 2M
     */
    public static final long USER_FILE_MAX_SIZE = 2 * 1024 * 1024L;

    /**
     * 图表上传文件后缀白名单
     */
    public static final List<String> USER_VALID_FILE_SUFFIX = Arrays.asList("png", "jpg", "jpeg");
}

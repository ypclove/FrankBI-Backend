package com.frank.bi.model.dto.aiassistant;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 问答助手信息表
 *
 * @author Frank
 */
@Data
public class AiAssistantAddRequest implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 问题名称
     */
    private String questionName;

    /**
     * 问题概述
     */
    private String questionGoal;

    /**
     * 问答结果
     */
    private String questionResult;

    /**
     * 问题类型
     */
    private String questionType;
}
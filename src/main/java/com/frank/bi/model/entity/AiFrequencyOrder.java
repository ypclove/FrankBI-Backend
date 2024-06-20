package com.frank.bi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 次数订单表
 *
 * @author Frank
 */
@Data
@TableName(value = "ai_frequency_order")
public class AiFrequencyOrder implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 购买数量
     */
    private Long purchaseQuantity;

    /**
     * 单价
     */
    private Double price;

    /**
     * 交易金额
     */
    @Setter
    private Double totalAmount;

    /**
     * 交易状态
     * 0：待付款
     * 1：已完成
     * 2：无效订单
     */
    private Integer orderStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}
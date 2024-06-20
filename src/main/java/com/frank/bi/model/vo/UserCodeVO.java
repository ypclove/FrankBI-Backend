package com.frank.bi.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户编号
 *
 * @author Frank
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserCodeVO extends UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
}
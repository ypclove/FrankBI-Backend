package com.frank.bi.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author Frank
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
}
package com.frank.bi.model.dto.frequency;

import lombok.Data;

import java.io.Serializable;

/**
 * 使用次数
 *
 * @author Frank
 */
@Data
public class FrequencyRequest implements Serializable {

    /**
     * 使用次数
     */
    private int frequency;
}

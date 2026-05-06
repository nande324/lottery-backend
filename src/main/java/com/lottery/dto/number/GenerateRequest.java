package com.lottery.dto.number;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 号码生成请求 DTO
 */
@Data
public class GenerateRequest {

    /** 彩票模式ID */
    @NotNull(message = "彩票模式ID不能为空")
    private Long modeId;

    /** 生成注数（1-100，默认为1） */
    @Min(value = 1, message = "生成注数最少为1注")
    @Max(value = 100, message = "生成注数最多为100注")
    private int count = 1;
}

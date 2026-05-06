package com.lottery.dto.number;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 号码校验请求 DTO
 */
@Data
public class ValidateRequest {

    /** 彩票模式ID */
    @NotNull(message = "彩票模式ID不能为空")
    private Long modeId;

    /** 红球号码列表 */
    private List<Integer> redNumbers;

    /** 蓝球号码列表 */
    private List<Integer> blueNumbers;
}

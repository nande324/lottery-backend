package com.lottery.dto.defaultnumber;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建默认号码请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultNumberCreateRequest {

    /** 所属彩票模式ID */
    @NotNull(message = "彩票模式ID不能为空")
    private Long modeId;

    /** 默认号码名称 */
    @NotBlank(message = "默认号码名称不能为空")
    private String name;

    /** 红球号码，逗号分隔 */
    @NotBlank(message = "红球号码不能为空")
    private String redNumbers;

    /** 蓝球号码，逗号分隔（可为空） */
    private String blueNumbers;
}

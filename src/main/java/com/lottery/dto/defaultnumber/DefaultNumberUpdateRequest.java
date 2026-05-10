package com.lottery.dto.defaultnumber;

import lombok.*;

/**
 * 更新默认号码请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultNumberUpdateRequest {

    /** 默认号码名称 */
    private String name;

    /** 红球号码，逗号分隔 */
    private String redNumbers;

    /** 蓝球号码，逗号分隔（可为空） */
    private String blueNumbers;
}

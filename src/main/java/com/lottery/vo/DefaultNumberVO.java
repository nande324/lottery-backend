package com.lottery.vo;

import com.lottery.entity.DefaultNumber;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 默认号码视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultNumberVO {

    /** 主键 */
    private Long id;

    /** 所属彩票模式ID */
    private Long modeId;

    /** 彩票模式名称 */
    private String modeName;

    /** 默认号码名称 */
    private String name;

    /** 红球号码 */
    private String redNumbers;

    /** 蓝球号码 */
    private String blueNumbers;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /**
     * 从实体转换为VO
     *
     * @param entity 实体对象
     * @return VO对象
     */
    public static DefaultNumberVO fromEntity(DefaultNumber entity) {
        if (entity == null) {
            return null;
        }
        return DefaultNumberVO.builder()
                .id(entity.getId())
                .modeId(entity.getModeId())
                .name(entity.getName())
                .redNumbers(entity.getRedNumbers())
                .blueNumbers(entity.getBlueNumbers())
                .createdTime(entity.getCreatedTime())
                .build();
    }

    /**
     * 从实体转换为VO（带模式名称）
     *
     * @param entity   实体对象
     * @param modeName 模式名称
     * @return VO对象
     */
    public static DefaultNumberVO fromEntity(DefaultNumber entity, String modeName) {
        DefaultNumberVO vo = fromEntity(entity);
        if (vo != null) {
            vo.setModeName(modeName);
        }
        return vo;
    }
}

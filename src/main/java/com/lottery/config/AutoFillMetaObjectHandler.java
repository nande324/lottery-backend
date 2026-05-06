package com.lottery.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 自动填充 createdTime 和 updatedTime 字段
 */
@Slf4j
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * 填充 createdTime 和 updatedTime 为当前时间
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("自动填充插入时间字段");
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, now);
    }

    /**
     * 更新时自动填充
     * 填充 updatedTime 为当前时间
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("自动填充更新时间字段");
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }
}


package com.lottery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 彩票管理系统后端启动类
 */
@SpringBootApplication(scanBasePackages = "com.lottery")
@MapperScan("com.lottery.mapper")
public class LotteryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryApplication.class, args);
    }
}

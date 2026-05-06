package com.lottery.service.wincheck;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * WinChecker 工厂类
 * 根据彩票模式编码（LotteryMode.code）返回对应的 WinChecker 实现
 */
@Component
@RequiredArgsConstructor
public class WinCheckerFactory {

    private final SsqWinChecker ssqWinChecker;
    private final DltWinChecker dltWinChecker;
    private final QlcWinChecker qlcWinChecker;
    private final Pl3WinChecker pl3WinChecker;
    private final Pl5WinChecker pl5WinChecker;

    /**
     * 根据彩票模式编码获取对应的 WinChecker
     *
     * @param modeCode 彩票模式编码（如 SSQ、DLT、QLC、PL3、PL5）
     * @return 对应的 WinChecker 实现
     * @throws IllegalArgumentException 若模式编码不支持中奖核对则抛出
     */
    public WinChecker getChecker(String modeCode) {
        if (modeCode == null) {
            throw new IllegalArgumentException("彩票模式编码不能为空");
        }
        switch (modeCode.toUpperCase()) {
            case "SSQ":
                return ssqWinChecker;
            case "DLT":
                return dltWinChecker;
            case "QLC":
                return qlcWinChecker;
            case "PL3":
                return pl3WinChecker;
            case "PL5":
                return pl5WinChecker;
            default:
                throw new IllegalArgumentException("不支持的彩票模式：" + modeCode + "，暂不支持中奖核对");
        }
    }
}

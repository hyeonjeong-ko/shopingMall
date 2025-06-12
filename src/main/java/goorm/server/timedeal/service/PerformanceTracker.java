package goorm.server.timedeal.service;

import goorm.server.timedeal.logging.AppLogger;
import lombok.RequiredArgsConstructor;

// 성능 추적을 위한 유틸리티 클래스
@RequiredArgsConstructor
private static class PerformanceTracker {
    private long startTime;

    public void start(String operationName) {
        startTime = System.nanoTime();
    }

    public void end(String operationName, Object... params) {
        long endTime = System.nanoTime();
        AppLogger.logPerformance(operationName, endTime - startTime, params);
    }
}

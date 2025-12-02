package com.rdc.weflow_server.entity.step;

/**
 * Step 종류(요구사항 정의, 화면 설계, 디자인, 퍼블리싱, 개발, 검수).
 */
public enum StepCategory {
    REQUIREMENTS,      // 요구사항 정의
    UI_PLANNING,       // 화면 설계
    DESIGN,            // 디자인
    PUBLISHING,        // 퍼블리싱
    DEVELOPMENT,       // 개발
    QA                 // 검수
}

-- posts 300만 건 더미 생성 스크립트
-- 전제: 02_numbers_table.sql로 numbers 테이블(0~999,999)이 준비되어 있음
-- 목적: OFFSET vs Cursor 페이징 성능 비교, 인덱스 적용 전/후 EXPLAIN 분석용 대량 데이터 구성
-- 특징: DB 내부 INSERT ... SELECT 한 번으로 300만 건 생성, created_at은 1년 범위로 분산
-- 주의: 테스트/성능 검증 전용. 실제 서비스 데이터는 TRUNCATE로 삭제되므로 운영 DB 금지.

USE weflow_local;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE posts;

-- 300만 생성: numbers(0~999,999) 3회 결합
INSERT INTO posts (
    title, content,
    project_phase, status, open_status,
    parent_post_id,
    step_id, user_id,
    created_at, updated_at, deleted_at
)
SELECT
    CONCAT('테스트 게시글 ', t.n),
    CONCAT('대용량 성능 테스트용 본문 ', t.n),

    -- project_phase: @Enumerated 누락 → ORDINAL(숫자) 저장, IN_PROGRESS = 1
    1             AS project_phase,
    'NORMAL'      AS status,          -- NOT NULL, 문자열 ENUM
    'OPEN'        AS open_status,     -- NOT NULL, 문자열 ENUM

    NULL AS parent_post_id,
    1    AS step_id,
    1    AS user_id,

    NOW() - INTERVAL (t.n % 365) DAY AS created_at, -- 1년 분산
    NOW() AS updated_at,
    NULL  AS deleted_at
FROM (
         SELECT n FROM numbers
         UNION ALL
         SELECT n + 1000000 FROM numbers
         UNION ALL
         SELECT n + 2000000 FROM numbers
     ) t;

SET FOREIGN_KEY_CHECKS = 1;

-- 검증
# SELECT COUNT(*) AS posts_count FROM posts;

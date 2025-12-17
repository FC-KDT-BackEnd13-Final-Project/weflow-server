/* =========================================================
   Activity Logs – Cursor Pagination Test Data
   목적:
   - 커서 기반 페이징(created_at + id) 검증
   - 대량 데이터 + created_at 동률 케이스 포함
   - 로컬 DB (weflow_local) 기준
   ========================================================= */

USE weflow_local;

START TRANSACTION;

/* ---------------------------------------------------------
   1. 기존 activity_logs 비우기 (선택)
   ※ 완전 초기화 원하면 사용
--------------------------------------------------------- */
-- DELETE FROM activity_logs;
-- ALTER TABLE activity_logs AUTO_INCREMENT = 1;


/* ---------------------------------------------------------
   2. 게시글(posts) 기반 로그 대량 생성
   - posts 테이블에 이미 300만 건 존재한다고 가정
   - 처음엔 10만 건만 생성 (필요 시 LIMIT 증가)
--------------------------------------------------------- */
INSERT INTO activity_logs (
    action_type,
    target_table,
    target_id,
    ip_address,
    user_id,
    project_id,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    'CREATE'              AS action_type,
    'POST'                AS target_table,
    p.id                  AS target_id,
    '127.0.0.1'           AS ip_address,
    1                     AS user_id,
    1                     AS project_id,
    p.created_at          AS created_at,
    p.created_at          AS updated_at,
    NULL                  AS deleted_at
FROM posts p
ORDER BY p.id
LIMIT 100000;


/* ---------------------------------------------------------
   3. created_at 동률 데이터 강제 생성
   - 커서 조건 (created_at, id) 안정성 검증용
--------------------------------------------------------- */
UPDATE activity_logs
SET created_at = '2025-12-16 10:00:00.123456'
WHERE id IN (
    SELECT id FROM (
                       SELECT id
                       FROM activity_logs
                       ORDER BY id DESC
                       LIMIT 10000
                   ) t
);


/* ---------------------------------------------------------
   4. 커서 페이징용 인덱스 생성
--------------------------------------------------------- */

-- 기본 커서 조회용 (전체 로그)
CREATE INDEX idx_activity_logs_created_id
    ON activity_logs (created_at DESC, id DESC);

-- 프로젝트 필터 포함 커서 조회용 (실무 최다 케이스)
CREATE INDEX idx_activity_logs_project_created_id
    ON activity_logs (project_id, created_at DESC, id DESC);


COMMIT;


/* ---------------------------------------------------------
   5. 검증용 쿼리 예시 (참고)
--------------------------------------------------------- */

-- 최초 페이지
-- SELECT *
-- FROM activity_logs
-- ORDER BY created_at DESC, id DESC
-- LIMIT 20;

-- 다음 페이지 (커서 기반)
-- SELECT *
-- FROM activity_logs
-- WHERE
--   created_at < '2025-12-16 10:00:00.123456'
--   OR (
--       created_at = '2025-12-16 10:00:00.123456'
--       AND id < 123456
--   )
-- ORDER BY created_at DESC, id DESC
-- LIMIT 20;


-- mysql -h 127.0.0.1 -P 3307 -u root -p weflow_local < 02_activity_logs_cursor_test.sql
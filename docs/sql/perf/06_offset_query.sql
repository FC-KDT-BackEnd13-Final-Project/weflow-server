-- OFFSET 페이징 성능 측정(인덱스 적용 후)
--  - 내용: EXPLAIN / EXPLAIN ANALYZE로 Page1/중간/후반 OFFSET 성능 확인
--  - 데이터 전제: posts 3,000,000 / step_id=1 존재 / deleted_at IS NULL / 정렬 created_at DESC, id DESC
--  - 실행 팁: 각 쿼리 워밍업 후 EXPLAIN ANALYZE의 actual time을 기록
-- 기록 가이드: Q1/Q2/Q3 각각 for-page(OFFSET 0/50만/200만) 쿼리의 실제 소요 시간을 표로 메모해 커서 대비 비교

USE weflow_local;

-- ------------------------------------------------------------
-- (선택) 현재 인덱스 확인
-- ------------------------------------------------------------
SHOW INDEX FROM posts;

-- ------------------------------------------------------------
-- 06) OFFSET After 측정 (인덱스 적용 상태)
--    - Q1/Q2/Q3 각각:
--      1) EXPLAIN (플랜 스냅샷)
--      2) EXPLAIN ANALYZE (실제 수행시간/rows 포함)
-- ------------------------------------------------------------

-- 06-Q1 (OFFSET 0)
EXPLAIN
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 0;

EXPLAIN ANALYZE
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 0;

-- 06-Q2 (OFFSET 500,000)
EXPLAIN
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 500000;

EXPLAIN ANALYZE
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 500000;

-- 06-Q3 (OFFSET 2,000,000)
EXPLAIN
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 2000000;

EXPLAIN ANALYZE
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 2000000;

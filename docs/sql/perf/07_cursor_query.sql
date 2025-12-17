-- 커서(Seek) 페이징 성능 측정용 쿼리
--  - 목적: EXPLAIN / EXPLAIN ANALYZE로 커서 방식(Page1 → Page2 → Page3) 성능 확인
--  - 전제: posts 3,000,000 / step_id=1 존재 / deleted_at IS NULL / 정렬 created_at DESC, id DESC
--  - 권장 인덱스: (step_id, deleted_at, created_at DESC, id DESC)
--  - 측정 팁: EXPLAIN ANALYZE 결과의 Limit 단계 actual time을 기록하고, 1~2회 워밍업 후 반복 실행 값의 중앙값/최소값을 메모
-- 사용법:
--  1) Page1 실행 후 마지막 행의 created_at/id를 @cursor 변수에 수동 대입
--  2) Page2/Page3를 동일 패턴으로 반복하며 실제 소요 시간 기록
--  3) OFFSET 측정 결과(06)와 비교하여 커서가 깊은 페이지에서 어떻게 개선되는지 확인

USE weflow_local;

-- ------------------------------------------------------------
-- (선택) 인덱스 확인
-- ------------------------------------------------------------
SHOW INDEX FROM posts;

-- ------------------------------------------------------------
-- 07-Page1: 첫 페이지(커서 없음)
-- ------------------------------------------------------------
EXPLAIN
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5;

EXPLAIN ANALYZE
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5;

-- (수동) 위 SELECT 결과의 "마지막 row" 값을 아래에 입력
-- 예) 마지막 row: created_at='2025-12-15 09:37:45', id=2998476
SET @cursorCreatedAt = '2025-12-15 09:37:45';
SET @cursorId = 2998476;

-- ------------------------------------------------------------
-- 07-Page2: 다음 페이지(Seek)
-- 조건:
--  - created_at 더 과거이거나
--  - created_at이 같으면 id가 더 작은 것
-- ------------------------------------------------------------
EXPLAIN
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
  AND (
    p.created_at < @cursorCreatedAt
        OR (p.created_at = @cursorCreatedAt AND p.id < @cursorId)
    )
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5;

EXPLAIN ANALYZE
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
  AND (
    p.created_at < @cursorCreatedAt
        OR (p.created_at = @cursorCreatedAt AND p.id < @cursorId)
    )
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5;

-- (수동) Page2 결과의 마지막 row로 커서 갱신 후 Page3 실행
SET @cursorCreatedAt = '2025-12-15 09:37:45';
SET @cursorId = 2996651;

-- ------------------------------------------------------------
-- 07-Page3: 다음 페이지(Seek) - Page2와 동일 쿼리 반복
-- ------------------------------------------------------------
EXPLAIN
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
  AND (
    p.created_at < @cursorCreatedAt
        OR (p.created_at = @cursorCreatedAt AND p.id < @cursorId)
    )
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5;

EXPLAIN ANALYZE
SELECT p.id, p.created_at
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
  AND (
    p.created_at < @cursorCreatedAt
        OR (p.created_at = @cursorCreatedAt AND p.id < @cursorId)
    )
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5;

-- ------------------------------------------------------------
-- (선택) Deep Cursor 테스트
-- - OFFSET 점프가 불가능하므로, "N번 next" 반복 실행으로 deep 상황을 만든다.
-- - 성능 기록은 PageN에서도 Page1/2와 유사한지 확인.
-- ------------------------------------------------------------

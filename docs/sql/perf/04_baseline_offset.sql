-- 인덱스 적용 전(베이스라인) OFFSET 페이징 실행 계획 확인용
-- A) step_id OFFSET, B) project_id JOIN OFFSET, C) project_id+phase OFFSET
-- EXPLAIN 결과와 실제 소요 시간은 별도 메모
-- 사용법:
--  1) 각 섹션을 실행해 EXPLAIN 결과를 기록
--  2) Workbench 등에서 실제 실행 시간을 메모(Offset 커질수록 악화 확인용)
--  3) 05_indexes.sql 적용 전/후 결과 비교

-- A) step_id 기반 (가장 중요)
EXPLAIN
SELECT p.*
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC
    LIMIT 5 OFFSET 0;

EXPLAIN
SELECT p.*
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC
    LIMIT 5 OFFSET 500000;

EXPLAIN
SELECT p.*
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC
    LIMIT 5 OFFSET 2000000;


-- Q1: 첫 페이지
EXPLAIN
SELECT p.*
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 0;

-- Q2: 중간 페이지
EXPLAIN
SELECT p.*
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 500000;

-- Q3: 뒤 페이지
EXPLAIN
SELECT p.*
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 2000000;


-- B) project_id 기반 (join 포함)
EXPLAIN
SELECT p.*
FROM posts p
         JOIN steps s ON p.step_id = s.id
WHERE s.project_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC
    LIMIT 5 OFFSET 0;


EXPLAIN
SELECT p.*
FROM posts p
         JOIN steps s ON p.step_id = s.id
WHERE s.project_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC
    LIMIT 5 OFFSET 2000000;

-- C) phase까지 넣는 버전
EXPLAIN
SELECT p.*
FROM posts p
         JOIN steps s ON p.step_id = s.id
WHERE s.project_id = 1
  AND p.project_phase = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC
    LIMIT 5 OFFSET 500000;

SELECT COUNT(*) FROM posts WHERE step_id=1 AND deleted_at IS NULL;

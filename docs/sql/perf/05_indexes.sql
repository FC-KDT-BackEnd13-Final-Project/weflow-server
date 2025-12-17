-- Posts 페이징 성능 개선용 인덱스
-- 목적: 04_baseline_offset.sql에서 filesort를 줄이고,
--       step_id / project_id JOIN / project_phase 조건의 정렬(created_at DESC, id DESC)까지 커버
--       이후 06_offset_query.sql / 07_cursor_query.sql 비교 시 "개선 상태"로 사용
-- 주의: 300만 건 기준 생성 시간 오래 걸릴 수 있음, 기존 인덱스 중복 여부를 SHOW INDEX로 확인
-- 확인 예: SHOW INDEX FROM posts; / SHOW INDEX FROM steps;
-- 팁: 생성 후 EXPLAIN에서 "Using filesort"가 사라졌는지, key 선택이 인덱스로 바뀌는지 확인

USE weflow_local;

-- (선택) 현재 인덱스 확인
SHOW INDEX FROM posts;
SHOW INDEX FROM steps;

-- 1) step_id 기반 조회/정렬 최적화
--  WHERE step_id = ? AND deleted_at IS NULL
--  ORDER BY created_at DESC, id DESC
--  를 한 번에 태우기 위한 복합 인덱스
CREATE INDEX idx_posts_step_del_created_id
    ON posts (step_id, deleted_at, created_at DESC, id DESC);

-- 2) project_phase(ORDINAL=tinyint)까지 같이 거는 경우 대비
--  WHERE step_id = ? AND project_phase = ? AND deleted_at IS NULL
--  ORDER BY created_at DESC, id DESC
CREATE INDEX idx_posts_step_phase_del_created_id
    ON posts (step_id, project_phase, deleted_at, created_at DESC, id DESC);

-- 3) project_id 조인 최적화(steps에서 project_id로 먼저 좁힐 수 있게)
--  WHERE s.project_id = ?
--  JOIN steps s ON p.step_id = s.id
CREATE INDEX idx_steps_project_id
    ON steps (project_id);

-- (선택) 인덱스 생성 후 확인
SHOW INDEX FROM posts;
SHOW INDEX FROM steps;

EXPLAIN
SELECT p.*
FROM posts p
WHERE p.step_id = 1
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC, p.id DESC
LIMIT 5 OFFSET 2000000;

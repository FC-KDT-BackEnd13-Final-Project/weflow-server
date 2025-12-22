/* =========================================================
   posts 목록 조회(OFFSET Pagination) 성능 개선 인덱스
   ---------------------------------------------------------
   대상 API:
   - GET /api/projects/{projectId}/posts
   - PostRepository 기반 게시글 목록 조회

   공통 쿼리 패턴:
   - posts p
     JOIN steps s ON p.step_id = s.id
     WHERE s.project_id = :projectId
       AND p.deleted_at IS NULL
       [AND p.step_id = :stepId]
       [AND p.project_phase = :projectPhase]
       [AND p.open_status = :openStatus]
     ORDER BY p.created_at DESC, p.id DESC
     LIMIT :size OFFSET :offset

   목적:
   - OFFSET 페이지네이션에서 filesort / 불필요한 스캔 최소화
   - stepId / projectPhase / openStatus 조합별 조회 성능 개선
   - 실제 사용되는 쿼리 패턴에 맞춰 최소 개수로 인덱스 구성

   주의:
   - MySQL 8 기준
   - 운영/RDS에서는 인덱스 생성 시 잠금/시간 소요 가능
   - 적용 전 SHOW INDEX FROM posts; 로 중복 여부 확인 권장
   ========================================================= */

-- =========================================================
-- 1) stepId 기반 조회
-- ---------------------------------------------------------
-- 사용 케이스:
--   WHERE p.step_id = ?
--     AND p.deleted_at IS NULL
--   ORDER BY p.created_at DESC, p.id DESC
--
-- 효과:
--   - 특정 단계 게시글 목록 조회
--   - 기본 정렬(createdAt DESC) + OFFSET 페이징 최적화
-- =========================================================
CREATE INDEX idx_posts_step_del_created_id
    ON posts (step_id, deleted_at, created_at DESC, id DESC);


-- =========================================================
-- 2) stepId + openStatus 조회
-- ---------------------------------------------------------
-- 사용 케이스:
--   WHERE p.step_id = ?
--     AND p.open_status = ?
--     AND p.deleted_at IS NULL
--   ORDER BY p.created_at DESC, p.id DESC
--
-- 효과:
--   - 단계별 + 공개/비공개(Open/Closed) 필터 조회
--   - openStatus 조건 포함 시 추가 스캔 제거
-- =========================================================
CREATE INDEX idx_posts_step_open_del_created_id
    ON posts (step_id, open_status, deleted_at, created_at DESC, id DESC);


-- =========================================================
-- 3) stepId + projectPhase (+ openStatus) 조회
-- ---------------------------------------------------------
-- 사용 케이스:
--   WHERE p.step_id = ?
--     AND p.project_phase = ?
--     [AND p.open_status = ?]
--     AND p.deleted_at IS NULL
--   ORDER BY p.created_at DESC, p.id DESC
--
-- 효과:
--   - 프로젝트 Phase 탭 + 단계 필터 조합 조회
--   - phase + openStatus 조건이 함께 오는 경우까지 커버
-- =========================================================
CREATE INDEX idx_posts_step_phase_open_del_created_id
    ON posts (step_id, project_phase, open_status, deleted_at, created_at DESC, id DESC);


-- =========================================================
-- (선택) 적용 결과 확인
-- =========================================================
-- SHOW INDEX FROM posts;
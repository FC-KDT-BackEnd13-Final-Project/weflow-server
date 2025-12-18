/* =========================================================
   Activity Logs – Cursor Pagination Indexes
   목적:
   - created_at DESC, id DESC 커서 정렬을 인덱스로 커버
   - project_id + created_at + id 필터/정렬까지 고려
   사용 환경:
   - 로컬 DB: weflow_local
   - 운영/개발: 환경에 맞춰 DB 명만 교체
   주의:
   - 동일 이름 인덱스 존재 여부를 먼저 확인 (SHOW INDEX FROM activity_logs;)
   - MySQL 8 기준
   ========================================================= */

USE weflow_local;

-- 전체 커서 조회용
CREATE INDEX idx_activity_logs_created_id
    ON activity_logs (created_at DESC, id DESC);

-- 프로젝트별 커서 조회 최적화
CREATE INDEX idx_activity_logs_project_created_id
    ON activity_logs (project_id, created_at DESC, id DESC);

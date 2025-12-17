# Activity Log Cursor & Performance Test SQL

## 개요
- 커서 페이징(`created_at DESC, id DESC`)의 **안정성·성능 검증**과  
  **대용량 더미 데이터 생성**을 위한 SQL 스크립트 모음입니다.
- 운영 DB 반영 전, **동일 인덱스 존재 여부**와 **데이터 규모 영향**을 반드시 확인하세요.

---

## Activity Logs (커서 페이징)

### 주요 스크립트
- `01_activity_logs_cursor_indexes.sql`
    - 커서 정렬(`created_at DESC, id DESC`)을 커버하는 인덱스 생성 스크립트
    - `project_id + created_at + id` 조회까지 고려
    - **운영/개발 환경 적용 후보**
    - 실행 전 `SHOW INDEX FROM activity_logs;`로 중복 여부 확인 필수

- `02_activity_logs_cursor_test.sql`
    - `posts` 테이블에 **300만 건이 존재한다는 전제**로 `activity_logs` 대량 삽입
    - `created_at` 동률 케이스 포함(커서 안정성 검증 목적)
    - **로컬/성능 테스트 전용**
    - ❌ 운영 DB 실행 금지

---

## perf 하위 스크립트 (게시글 페이징 성능 비교)

- `01_seed_check.sql`
    - users / steps 기본 시드 상태 확인용 조회

- `02_numbers_table.sql`
    - `0 ~ 999,999` 숫자를 담는 `numbers` 테이블 생성
    - 대용량 더미 데이터 생성을 위한 카운터 테이블

- `03_posts_bulk_insert.sql`
    - `numbers` 테이블을 활용해 `posts` 300만 건 더미 생성
    - `created_at`은 최근 1년 기준으로 분산

- `04_baseline_offset.sql`
    - 인덱스 적용 전 OFFSET 페이징 성능 비교용 쿼리 메모

- `05_indexes.sql`
    - OFFSET / CURSOR 성능 테스트 전 적용할 인덱스 정의

- `06_offset_query.sql`
    - OFFSET 페이징 실행 계획 및 성능 확인용 쿼리 샘플

- `07_cursor_query.sql`
    - 커서(Seek) 페이징 실행 계획 및 성능 확인용 쿼리 샘플

---

## 실행 전 유의사항
- **데이터 규모**
    - 300만 건 삽입으로 DB 용량·시간 소모가 큼
    - 로컬 또는 성능 테스트 환경 전용으로 사용 권장

- **인덱스 중복**
    - 기존 인덱스와 이름/구성이 겹치면 생성 실패 또는 중복 가능
    - 실행 전 반드시 `SHOW INDEX FROM ...` 확인

- **환경**
    - 기본 DB는 `weflow_local` 기준
    - 개발/운영 환경 적용 시 DB명 수정 필요

---

## 운영 DB 적용 가이드
- ❌ **실행 금지**
    - `02_activity_logs_cursor_test.sql`
    - `docs/sql/perf/*` (numbers / posts 300만 생성 / 성능 테스트 쿼리)

- ✅ **검토 후 적용 가능**
    - `01_activity_logs_cursor_indexes.sql`
    - 인덱스 중복 여부 확인 후 트래픽 저점에 적용
    - 필요 시 사전 백업 및 `EXPLAIN` 검토 권장
# Activity Log Cursor Pagination – SQL Docs

## 개요

이 디렉터리는 **Activity Log 목록 조회에 커서(Seek) 페이징을 도입하기 위한**
인덱스 설계 근거와 적용 스크립트를 정리한 문서입니다.

- 대용량 로그 데이터 환경에서 **OFFSET 페이징의 성능 한계**를 확인하고
- `created_at DESC, id DESC` 기준의 **Cursor(Seek) 페이징을 안정적으로 지원**하기 위한
  인덱스를 정의합니다.

** 본 디렉터리는 서비스 코드 실행에 필수적인 SQL 모음이 아니라, **설계 검증 및 운영 반영을 위한 참고 문서 성격**의 자료입니다.

---

## 포함된 파일

### `01_activity_logs_cursor_indexes.sql`

Activity Log 커서 페이징을 위한 **인덱스 정의 스크립트**

#### 목적
- 커서 정렬 기준인 `(created_at DESC, id DESC)`를 인덱스로 커버
- `project_id` 필터 + 커서 정렬을 함께 사용하는  
  **실제 서비스 조회 패턴 최적화**

#### 포함 인덱스
- 전체 로그 커서 조회용  
  `(created_at DESC, id DESC)`
- 프로젝트별 로그 커서 조회용  
  `(project_id, created_at DESC, id DESC)`

#### 적용 대상
- Activity Log 목록 조회 API
- 관리자 로그 / 프로젝트별 로그 화면
- 최신순 정렬 기반 페이지네이션

#### 적용 전 주의사항
- 기존 인덱스 중복 여부 반드시 확인
  ```sql
  SHOW INDEX FROM activity_logs;
- MySQL 8.x 기준 작성
- 트래픽 저점 시간대 적용 권장

#### 적용 환경 안내
- 기본 DB명: weflow_local
- 개발 / 운영 환경에서는 DB명만 환경에 맞게 수정하여 사용
```sql
  USE weflow_local;

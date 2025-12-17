-- 시드/기본 데이터 상태를 빠르게 확인하는 간단한 체크리스트
-- 목적: 대량 데이터 삽입/성능 테스트 전에 최소한의 레퍼런스 데이터가 있는지 확인
-- 실행/확인:
--  1) users 테이블에 계정이 존재하는지
--  2) project_id=1 기준 steps 개수
--  3) steps 정렬(order_index) 상태

-- 1) 사용자 수
SELECT COUNT(*) FROM users;

-- 2) project_id=1 기준 step 개수
SELECT COUNT(*) FROM steps WHERE project_id=1;

-- 3) project_id=1에 속한 step의 순서/제목 확인
SELECT id, order_index, title FROM steps WHERE project_id=1 ORDER BY order_index;

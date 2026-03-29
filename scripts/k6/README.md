# Coupon k6 발급 부하테스트

`scripts/k6/docker-compose.yml` 기준으로 3가지 시나리오를 분리했습니다.

추가 문서
- 상세 흐름 정리: `scripts/k6/K6_FLOW.md`
- 단계형 VUS 비교 가이드: `scripts/k6/K6_VUS_LADDER.md`

## 1) 시나리오 A: 쿠폰 10,000장
- 목적: 재고가 충분할 때 처리량/지연(p95, p99) 확인

```bash
K6_BASE_URL=http://host.docker.internal:8083 \
docker compose -f scripts/k6/docker-compose.yml run --rm k6-issue-large-stock
```

기본값
- `COUPON_TOTAL_QUANTITY=10000`
- `ISSUE_ATTEMPTS=10000`
- `VUS=200`

---

## 2) 시나리오 B: 선착순 10장에 5,000명 경쟁
- 목적: 초과 발급 방지(정합성) + 경합 시 지연 확인

```bash
K6_BASE_URL=http://host.docker.internal:8083 \
docker compose -f scripts/k6/docker-compose.yml run --rm k6-issue-hot-race
```

기본값
- `COUPON_TOTAL_QUANTITY=10`
- `ISSUE_ATTEMPTS=5000`
- `VUS=300`

---

## 3) 시나리오 C: 단계형 VUS 비교
- 목적: `200 → 300 → 500 → 1000 → 2000 → 5000 → 10000` 순서로 부하를 올리며 비교

```bash
K6_BASE_URL=http://host.docker.internal:8083 \
bash scripts/k6/run-vus-ladder.sh
```

기본값
- `K6_VUS_LEVELS="200 300 500 1000 2000 5000 10000"`
- `K6_LADDER_SERVICE=k6-issue-large-stock`
- 결과 파일: `scripts/k6/reports/vus-ladder-YYYYMMDD-HHMMSS.md`

---

## 공통 환경변수
- `K6_BASE_URL` (필수)
- `K6_COUPON_ID` (선택, 기존 쿠폰 사용)
- `K6_COUPON_TOTAL_QUANTITY` (선택, 쿠폰 생성 시 수량)
- `K6_ISSUE_ATTEMPTS` (선택, 발급 시도 횟수)
- `K6_VUS` (선택, 동시 사용자 수)
- `K6_USER_ID_BASE` (선택, 기본 1)
- `K6_STOCK_READ_RETRIES` (선택, 재고 조회 재시도 횟수, 기본 3)
- `K6_MAX_DURATION` (선택)
- `K6_THRESHOLD_P95_MS` (선택, issue API p95 임계값)
- `K6_THRESHOLD_P99_MS` (선택, issue API p99 임계값)
- `K6_VUS_LEVELS` (선택, 단계형 비교 실행 시 사용할 VUS 목록)
- `K6_LADDER_SERVICE` (선택, 단계형 비교에서 실행할 compose 서비스)
- `K6_REPORT_FILE` (선택, 단계형 비교 결과 Markdown 파일 경로)

기본 threshold
- LARGE_STOCK: `p95 < 1200ms`, `p99 < 2500ms`
- HOT_RACE: `p95 < 2500ms`, `p99 < 5000ms`

## 결과 확인 포인트
- `oversoldByAccepted` / `oversoldByStock` 가 `false`인지
- `teardownFinalStock`, `latestObservedFinalStock`, `finalStock` 을 같이 보고 teardown 시점 스냅샷이 이른지 확인
- `http_req_duration p95 / p99`
- `failureConflict(409)` 비율

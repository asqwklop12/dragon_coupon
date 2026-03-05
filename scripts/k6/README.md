# Coupon k6 순차 발급 테스트 (별도 Docker Compose)

앱용 `docker-compose.yml`와 분리해서,  
`scripts/k6/docker-compose.yml`로만 k6를 실행합니다.

## 1) 실행 방법
```bash
docker compose -f scripts/k6/docker-compose.yml run --rm k6-sequential
```

## 2) 자주 쓰는 커스텀 옵션

### 순차 발급 (예: 3회)
```bash
K6_ISSUE_COUNT=3 K6_USER_ID_BASE=1 \
docker compose -f scripts/k6/docker-compose.yml run --rm k6-sequential
```

## 3) 환경변수

- `K6_BASE_URL` (기본: `http://host.docker.internal:8083`)
- `K6_ISSUE_COUNT` (기본: `3`, 허용: `2`~`3`)
- `K6_USER_ID_BASE` (기본: `1`)
- `K6_MAX_DURATION` (기본: `30s`)

동작 방식:
- setup 단계에서 테스트용 쿠폰 1개를 생성
- 이후 발급 API를 2~3회 순차 호출 (동시성 없음)

> `K6_BASE_URL`은 실행 시 꼭 지정하세요.
```bash
K6_BASE_URL=http://localhost:8083 \
docker compose -f scripts/k6/docker-compose.yml run --rm k6-sequential
```

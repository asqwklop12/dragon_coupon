import http from 'k6/http';
import {Counter, Gauge} from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8083';
const COUPON_ID = readOptionalPositiveIntEnv('COUPON_ID');
const ISSUE_COUNT = readPositiveIntEnv('ISSUE_COUNT', 3);
const VUS = readPositiveIntEnv('VUS', 1);
const USER_ID_BASE = readPositiveIntEnv('USER_ID_BASE', 1);
const MAX_DURATION = __ENV.MAX_DURATION || '30s';

const issueSuccessCounter = new Counter('issue_success');
const issueFailureCounter = new Counter('issue_failure');
const initialStockGauge = new Gauge('coupon_stock_initial');
const finalStockGauge = new Gauge('coupon_stock_final');

export const options = {
  scenarios: {
    coupon_issue_sequential_test: {
      executor: 'shared-iterations',
      vus: VUS,
      iterations: ISSUE_COUNT,
      maxDuration: MAX_DURATION,
    },
  },
};

function readPositiveIntEnv(name, fallback) {
  const raw = __ENV[name];
  const value = raw === undefined ? fallback : Number(raw);
  if (!Number.isInteger(value) || value < 1) {
    throw new Error(`Invalid ${name}: ${raw}`);
  }
  return value;
}

function readOptionalPositiveIntEnv(name) {
  const raw = __ENV[name];
  if (raw === undefined || raw === '') {
    return null;
  }
  const value = Number(raw);
  if (!Number.isInteger(value) || value < 1) {
    throw new Error(`Invalid ${name}: ${raw}`);
  }
  return value;
}

function createCoupon() {
  const now = new Date();
  const startDate = new Date(now.getTime() - 60 * 1000).toISOString();
  const endDate = new Date(now.getTime() + 24 * 60 * 60 * 1000).toISOString();

  const response = http.post(
    `${BASE_URL}/api/coupons`,
    JSON.stringify({
      name: `k6-선착순-테스트-${Date.now()}`,
      description: 'k6 순차 발급 테스트용 쿠폰',
      couponType: 'FIXED_AMOUNT',
      status: 'ACTIVE',
      discountValue: 1000,
      minOrderAmount: 10000,
      maxDiscountAmount: 1000,
      totalQuantity: ISSUE_COUNT,
      validDays: 7,
      startDate,
      endDate,
    }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { type: 'create_coupon' },
    },
  );

  if (response.status !== 200 && response.status !== 201) {
    throw new Error(`[createCoupon] failed status=${response.status}, body=${response.body}`);
  }

  const body = response.json();
  const couponId = body?.data?.couponId;
  if (!couponId) {
    throw new Error(`[createCoupon] invalid response body=${response.body}`);
  }
  return Number(couponId);
}

function readRemainingStock(couponId) {
  const response = http.get(`${BASE_URL}/api/coupons/${couponId}/stock`, {
    tags: { type: 'stock_check' },
  });
  if (response.status !== 200) {
    console.warn(`[stock] 조회 실패 status=${response.status}`);
    return null;
  }
  const body = response.json();
  return body?.data?.remainingQuantity ?? null;
}

export function setup() {
  const couponId = COUPON_ID ?? createCoupon();
  const couponSource = COUPON_ID ? 'existing' : 'created';
  const initialStock = readRemainingStock(couponId);
  if (initialStock !== null) {
    initialStockGauge.add(initialStock);
  }
  console.log(
    `[setup] couponId=${couponId}, source=${couponSource}, issueCount=${ISSUE_COUNT}, initialStock=${initialStock}`,
  );
  return { couponId, initialStock, couponSource };
}

export default function (data) {
  const userId = USER_ID_BASE + __ITER + (__VU - 1);
  const response = http.post(
    `${BASE_URL}/api/coupons/${data.couponId}/issue`,
    JSON.stringify({ userId }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { type: 'issue' },
    },
  );

  let issued = false;
  if (response.status >= 200 && response.status < 300) {
    const body = response.json();
    issued = body?.success === true && body?.data?.issuedCouponId != null;
  }

  if (issued) {
    issueSuccessCounter.add(1);
    return;
  }

  issueFailureCounter.add(1, { status: String(response.status) });
  console.warn(`[issue] failed status=${response.status}, body=${response.body}`);
}

export function teardown(data) {
  const finalStock = readRemainingStock(data.couponId);
  if (finalStock !== null) {
    finalStockGauge.add(finalStock);
  }
  console.log(
    `[teardown] couponId=${data.couponId}, source=${data.couponSource}, initialStock=${data.initialStock}, finalStock=${finalStock}`,
  );
}

function readMetricCount(data, metricName) {
  return Number(data.metrics?.[metricName]?.values?.count ?? 0);
}

function readGaugeValue(data, metricName) {
  const value = data.metrics?.[metricName]?.values?.value;
  return value === undefined ? null : Number(value);
}

export function handleSummary(data) {
  const success = readMetricCount(data, 'issue_success');
  const failure = readMetricCount(data, 'issue_failure');
  const initialStock = readGaugeValue(data, 'coupon_stock_initial');
  const finalStock = readGaugeValue(data, 'coupon_stock_final');

  const summary = [
    '',
    '=== Coupon Issue Test Summary ===',
    `- baseUrl: ${BASE_URL}`,
    `- vus: ${VUS}`,
    `- issueCount: ${ISSUE_COUNT}`,
    `- success: ${success}`,
    `- failure: ${failure}`,
    `- initialStock: ${initialStock}`,
    `- finalStock: ${finalStock}`,
    '===========================================',
    '',
  ].join('\n');

  return { stdout: summary };
}

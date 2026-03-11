import http from 'k6/http';
import exec from 'k6/execution';
import {Counter, Gauge} from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8083';
const TEST_CASE = (__ENV.TEST_CASE || 'LARGE_STOCK').toUpperCase();

const CASE_PRESET = {
  LARGE_STOCK: {
    totalQuantity: 10000,
    issueAttempts: 10000,
    vus: 200,
    maxDuration: '5m',
    thresholdP95Ms: 1200,
    thresholdP99Ms: 2500,
  },
  HOT_RACE: {
    totalQuantity: 10,
    issueAttempts: 5000,
    vus: 300,
    maxDuration: '3m',
    thresholdP95Ms: 2500,
    thresholdP99Ms: 5000,
  },
};

const CASE_CONFIG = CASE_PRESET[TEST_CASE];
if (!CASE_CONFIG) {
  throw new Error(`Invalid TEST_CASE: ${TEST_CASE}. Use LARGE_STOCK or HOT_RACE.`);
}

const COUPON_ID = readOptionalPositiveIntEnv('COUPON_ID');
const COUPON_TOTAL_QUANTITY = readPositiveIntEnv('COUPON_TOTAL_QUANTITY', CASE_CONFIG.totalQuantity);
const ISSUE_ATTEMPTS = readPositiveIntEnv('ISSUE_ATTEMPTS', CASE_CONFIG.issueAttempts);
const VUS = readPositiveIntEnv('VUS', CASE_CONFIG.vus);
const USER_ID_BASE = readPositiveIntEnv('USER_ID_BASE', 1);
const MAX_DURATION = __ENV.MAX_DURATION || CASE_CONFIG.maxDuration;
const THRESHOLD_P95_MS = readPositiveIntEnv('THRESHOLD_P95_MS', CASE_CONFIG.thresholdP95Ms);
const THRESHOLD_P99_MS = readPositiveIntEnv('THRESHOLD_P99_MS', CASE_CONFIG.thresholdP99Ms);

const issueSuccessCounter = new Counter('issue_success');
const issueFailureCounter = new Counter('issue_failure');
const issueConflictCounter = new Counter('issue_failure_conflict');
const issueBadRequestCounter = new Counter('issue_failure_bad_request');
const issueEtcFailureCounter = new Counter('issue_failure_other');

const initialStockGauge = new Gauge('coupon_stock_initial');
const finalStockGauge = new Gauge('coupon_stock_final');

export const options = {
  scenarios: {
    coupon_issue_load_test: {
      executor: 'shared-iterations',
      vus: VUS,
      iterations: ISSUE_ATTEMPTS,
      maxDuration: MAX_DURATION,
    },
  },
  thresholds: {
    'http_req_duration{type:issue}': [
      `p(95)<${THRESHOLD_P95_MS}`,
      `p(99)<${THRESHOLD_P99_MS}`,
    ],
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

function createCoupon(totalQuantity) {
  const now = new Date();
  const startDate = new Date(now.getTime() - 60 * 1000).toISOString();
  const endDate = new Date(now.getTime() + 24 * 60 * 60 * 1000).toISOString();

  const response = http.post(
    `${BASE_URL}/api/coupons`,
    JSON.stringify({
      name: `k6-${TEST_CASE}-coupon-${Date.now()}`,
      description: `k6 issue load test (${TEST_CASE})`,
      couponType: 'FIXED_AMOUNT',
      status: 'ACTIVE',
      discountValue: 1000,
      minOrderAmount: 10000,
      maxDiscountAmount: 1000,
      totalQuantity,
      validDays: 7,
      startDate,
      endDate,
    }),
    {
      headers: {'Content-Type': 'application/json'},
      tags: {type: 'create_coupon'},
    },
  );

  if (response.status !== 200 && response.status !== 201) {
    throw new Error(`[createCoupon] failed status=${response.status}, body=${response.body}`);
  }

  const couponId = response.json()?.data?.couponId;
  if (!couponId) {
    throw new Error(`[createCoupon] invalid response body=${response.body}`);
  }
  return Number(couponId);
}

function readRemainingStock(couponId) {
  const response = http.get(`${BASE_URL}/api/coupons/${couponId}/stock`, {
    tags: {type: 'stock_check'},
  });

  if (response.status !== 200) {
    console.warn(`[stock] 조회 실패 status=${response.status}, couponId=${couponId}`);
    return null;
  }

  return Number(response.json()?.data?.remainingQuantity ?? null);
}

export function setup() {
  const couponId = COUPON_ID ?? createCoupon(COUPON_TOTAL_QUANTITY);
  const couponSource = COUPON_ID ? 'existing' : 'created';
  const initialStock = readRemainingStock(couponId);

  if (initialStock !== null) {
    initialStockGauge.add(initialStock);
  }

  const expectedMaxSuccess = initialStock === null
    ? Math.min(COUPON_TOTAL_QUANTITY, ISSUE_ATTEMPTS)
    : Math.min(initialStock, ISSUE_ATTEMPTS);

  console.log(
    `[setup] case=${TEST_CASE}, couponId=${couponId}, source=${couponSource}, totalQuantity=${COUPON_TOTAL_QUANTITY}, attempts=${ISSUE_ATTEMPTS}, vus=${VUS}, initialStock=${initialStock}, expectedMaxSuccess=${expectedMaxSuccess}`,
  );

  return {couponId, couponSource, initialStock, expectedMaxSuccess};
}

export default function (data) {
  const globalIteration = exec.scenario.iterationInTest;
  const userId = USER_ID_BASE + globalIteration;

  const response = http.post(
    `${BASE_URL}/api/coupons/${data.couponId}/issue`,
    JSON.stringify({userId}),
    {
      headers: {'Content-Type': 'application/json'},
      tags: {type: 'issue', case: TEST_CASE},
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

  issueFailureCounter.add(1);
  if (response.status === 409) {
    issueConflictCounter.add(1);
  } else if (response.status === 400) {
    issueBadRequestCounter.add(1);
  } else {
    issueEtcFailureCounter.add(1);
  }
}

export function teardown(data) {
  const finalStock = readRemainingStock(data.couponId);
  if (finalStock !== null) {
    finalStockGauge.add(finalStock);
  }

  console.log(
    `[teardown] case=${TEST_CASE}, couponId=${data.couponId}, source=${data.couponSource}, initialStock=${data.initialStock}, finalStock=${finalStock}`,
  );
}

function readMetricCount(data, metricName) {
  return Number(data.metrics?.[metricName]?.values?.count ?? 0);
}

function readGaugeValue(data, metricName) {
  const value = data.metrics?.[metricName]?.values?.value;
  return value === undefined ? null : Number(value);
}

function readDuration(data, name) {
  const value = data.metrics?.http_req_duration?.values?.[name];
  return value === undefined ? null : Number(value);
}

export function handleSummary(data) {
  const success = readMetricCount(data, 'issue_success');
  const failure = readMetricCount(data, 'issue_failure');
  const failureConflict = readMetricCount(data, 'issue_failure_conflict');
  const failureBadRequest = readMetricCount(data, 'issue_failure_bad_request');
  const failureOther = readMetricCount(data, 'issue_failure_other');

  const initialStock = readGaugeValue(data, 'coupon_stock_initial');
  const finalStock = readGaugeValue(data, 'coupon_stock_final');
  const expectedMaxSuccess = initialStock === null
    ? Math.min(COUPON_TOTAL_QUANTITY, ISSUE_ATTEMPTS)
    : Math.min(initialStock, ISSUE_ATTEMPTS);

  const actualIssuedByStock = initialStock !== null && finalStock !== null
    ? initialStock - finalStock
    : null;

  const oversoldBySuccess = success > expectedMaxSuccess;
  const oversoldByStock = actualIssuedByStock !== null && actualIssuedByStock > expectedMaxSuccess;

  const p95 = readDuration(data, 'p(95)');
  const p99 = readDuration(data, 'p(99)');
  const avg = readDuration(data, 'avg');
  const max = readDuration(data, 'max');

  const summary = [
    '',
    '=== Coupon Issue Load Test Summary ===',
    `- testCase: ${TEST_CASE}`,
    `- baseUrl: ${BASE_URL}`,
    `- couponTotalQuantity: ${COUPON_TOTAL_QUANTITY}`,
    `- issueAttempts: ${ISSUE_ATTEMPTS}`,
    `- vus: ${VUS}`,
    `- success: ${success}`,
    `- failure: ${failure}`,
    `  - failureConflict(409): ${failureConflict}`,
    `  - failureBadRequest(400): ${failureBadRequest}`,
    `  - failureOther: ${failureOther}`,
    `- initialStock: ${initialStock}`,
    `- finalStock: ${finalStock}`,
    `- actualIssuedByStock: ${actualIssuedByStock}`,
    `- expectedMaxSuccess: ${expectedMaxSuccess}`,
    `- oversoldBySuccess: ${oversoldBySuccess}`,
    `- oversoldByStock: ${oversoldByStock}`,
    `- http_req_duration avg(ms): ${avg}`,
    `- http_req_duration p95(ms): ${p95}`,
    `- http_req_duration p99(ms): ${p99}`,
    `- http_req_duration max(ms): ${max}`,
    `- threshold p95(ms): < ${THRESHOLD_P95_MS}`,
    `- threshold p99(ms): < ${THRESHOLD_P99_MS}`,
    '======================================',
    '',
  ].join('\n');

  return {stdout: summary};
}

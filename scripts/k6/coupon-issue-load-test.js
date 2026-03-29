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
const STOCK_READ_RETRIES = readPositiveIntEnv('STOCK_READ_RETRIES', 3);
const MAX_DURATION = __ENV.MAX_DURATION || CASE_CONFIG.maxDuration;
const THRESHOLD_P95_MS = readPositiveIntEnv('THRESHOLD_P95_MS', CASE_CONFIG.thresholdP95Ms);
const THRESHOLD_P99_MS = readPositiveIntEnv('THRESHOLD_P99_MS', CASE_CONFIG.thresholdP99Ms);

const issueAcceptedCounter = new Counter('issue_success');
const issueFailureCounter = new Counter('issue_failure');
const issueConflictCounter = new Counter('issue_failure_conflict');
const issueBadRequestCounter = new Counter('issue_failure_bad_request');
const issueOtherCounter = new Counter('issue_failure_other');
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
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
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

function readRemainingStockWithRetry(couponId, attempts = STOCK_READ_RETRIES, phase = 'stock') {
  let minObservedStock = null;

  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    const remainingStock = readRemainingStock(couponId);
    if (remainingStock !== null) {
      minObservedStock = minObservedStock === null
        ? remainingStock
        : Math.min(minObservedStock, remainingStock);
      return minObservedStock;
    }

    console.warn(`[${phase}] 조회 재시도 실패 attempt=${attempt}/${attempts}, couponId=${couponId}`);
  }

  return minObservedStock;
}

export function setup() {
  const couponId = COUPON_ID ?? createCoupon(COUPON_TOTAL_QUANTITY);
  const couponSource = COUPON_ID ? 'existing' : 'created';
  const initialStock = readRemainingStock(couponId);

  if (initialStock !== null) {
    initialStockGauge.add(initialStock);
  }

  const expectedMaxAccepted = initialStock === null
    ? Math.min(COUPON_TOTAL_QUANTITY, ISSUE_ATTEMPTS)
    : Math.min(initialStock, ISSUE_ATTEMPTS);
  const expectedFinalStock = initialStock === null
    ? null
    : Math.max(initialStock - expectedMaxAccepted, 0);

  console.log(
    `[setup] case=${TEST_CASE}, couponId=${couponId}, source=${couponSource}, totalQuantity=${COUPON_TOTAL_QUANTITY}, attempts=${ISSUE_ATTEMPTS}, vus=${VUS}, initialStock=${initialStock}, expectedMaxAccepted=${expectedMaxAccepted}`,
  );

  return {couponId, couponSource, initialStock, expectedMaxAccepted, expectedFinalStock};
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

  let accepted = false;
  if (response.status >= 200 && response.status < 300) {
    const body = response.json();
    accepted = body?.success === true
      && body?.data?.couponId === data.couponId
      && body?.data?.userId === userId
      && body?.data?.requestedAt != null;
  }

  if (accepted) {
    issueAcceptedCounter.add(1);
    return;
  }

  issueFailureCounter.add(1);
  if (response.status === 409) {
    issueConflictCounter.add(1);
  } else if (response.status === 400) {
    issueBadRequestCounter.add(1);
  } else {
    issueOtherCounter.add(1);
  }
}

export function teardown(data) {
  const finalStock = readRemainingStockWithRetry(data.couponId, STOCK_READ_RETRIES, 'teardown');
  if (finalStock !== null) {
    finalStockGauge.add(finalStock);
  }

  console.log(
    `[teardown] case=${TEST_CASE}, couponId=${data.couponId}, source=${data.couponSource}, initialStock=${data.initialStock}, expectedFinalStock=${data.expectedFinalStock}, finalStock=${finalStock}`,
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

function resolveFinalStock(teardownFinalStock, latestObservedFinalStock) {
  if (teardownFinalStock !== null && latestObservedFinalStock !== null) {
    return latestObservedFinalStock <= teardownFinalStock
      ? {
        finalStock: latestObservedFinalStock,
        finalStockSource: 'handleSummary_http',
      }
      : {
        finalStock: teardownFinalStock,
        finalStockSource: 'metric',
      };
  }

  if (latestObservedFinalStock !== null) {
    return {
      finalStock: latestObservedFinalStock,
      finalStockSource: 'handleSummary_http',
    };
  }

  return {
    finalStock: teardownFinalStock,
    finalStockSource: teardownFinalStock !== null ? 'metric' : 'unavailable',
  };
}

export function handleSummary(data) {
  const accepted = readMetricCount(data, 'issue_success');
  const failure = readMetricCount(data, 'issue_failure');
  const failureConflict = readMetricCount(data, 'issue_failure_conflict');
  const failureBadRequest = readMetricCount(data, 'issue_failure_bad_request');
  const failureOther = readMetricCount(data, 'issue_failure_other');

  const couponId = Number(data.setup_data?.couponId ?? 0) || null;
  const initialStock = readGaugeValue(data, 'coupon_stock_initial');
  const teardownFinalStock = readGaugeValue(data, 'coupon_stock_final');
  const latestObservedFinalStock = couponId !== null
    ? readRemainingStockWithRetry(couponId, STOCK_READ_RETRIES, 'handleSummary')
    : null;
  const {finalStock, finalStockSource} = resolveFinalStock(
    teardownFinalStock,
    latestObservedFinalStock,
  );

  const expectedMaxAccepted = initialStock === null
    ? Math.min(COUPON_TOTAL_QUANTITY, ISSUE_ATTEMPTS)
    : Math.min(initialStock, ISSUE_ATTEMPTS);
  const actualIssuedByStock = initialStock !== null && finalStock !== null
    ? initialStock - finalStock
    : null;

  const oversoldByAccepted = accepted > expectedMaxAccepted;
  const oversoldByStock = actualIssuedByStock !== null && actualIssuedByStock > expectedMaxAccepted;

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
    `- accepted: ${accepted}`,
    `- failure: ${failure}`,
    `  - failureConflict(409): ${failureConflict}`,
    `  - failureBadRequest(400): ${failureBadRequest}`,
    `  - failureOther: ${failureOther}`,
    `- initialStock: ${initialStock}`,
    `- teardownFinalStock: ${teardownFinalStock}`,
    `- latestObservedFinalStock: ${latestObservedFinalStock}`,
    `- finalStock: ${finalStock}`,
    `- finalStockSource: ${finalStockSource}`,
    `- actualIssuedByStock: ${actualIssuedByStock}`,
    `- expectedMaxAccepted: ${expectedMaxAccepted}`,
    `- expectedFinalStock: ${data.setup_data?.expectedFinalStock ?? null}`,
    `- oversoldByAccepted: ${oversoldByAccepted}`,
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

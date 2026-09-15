import { Response } from 'express';

/**
 * RFC 7807 Problem Details for HTTP APIs
 */
export interface ProblemDetails {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  code?: string;
  invalidParams?: Array<{ name: string; reason: string }>;
  timestamp: string;
}

export function sendProblemDetails(
  res: Response,
  status: number,
  title: string,
  detail: string,
  instance: string,
  code?: string,
  invalidParams?: Array<{ name: string; reason: string }>
) {
  const problem: ProblemDetails = {
    type: `https://api.omni-platform.com/errors/${(code || title).toLowerCase().replace(/[^a-z0-9]/g, '-')}`,
    title,
    status,
    detail,
    instance,
    code,
    invalidParams,
    timestamp: new Date().toISOString(),
  };

  res.setHeader('Content-Type', 'application/problem+json');
  return res.status(status).json(problem);
}

/**
 * gRPC Status Code mapping helper
 */
export const GrpcStatus = {
  OK: 0,
  CANCELLED: 1,
  UNKNOWN: 2,
  INVALID_ARGUMENT: 3,
  DEADLINE_EXCEEDED: 4,
  NOT_FOUND: 5,
  ALREADY_EXISTS: 6,
  PERMISSION_DENIED: 7,
  RESOURCE_EXHAUSTED: 8,
  FAILED_PRECONDITION: 9,
  ABORTED: 10,
  OUT_OF_RANGE: 11,
  UNIMPLEMENTED: 12,
  INTERNAL: 13,
  UNAVAILABLE: 14,
  DATA_LOSS: 15,
  UNAUTHENTICATED: 16,
} as const;

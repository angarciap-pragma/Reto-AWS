package com.aws.practice_aws.infrastructure.in.rest.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String TRACE_ID_KEY = "traceId";
    private static final String UNKNOWN_VALUE = "-";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader(CORRELATION_ID_HEADER, traceId);

        long startNanos = System.nanoTime();
        String method = request.getMethod();
        String path = buildPath(request);
        String clientIp = resolveClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("http.request.started method={} path={} clientIp={} userAgent={}",
                method,
                path,
                clientIp,
                userAgent == null ? UNKNOWN_VALUE : userAgent);

        Throwable requestError = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable throwable) {
            requestError = throwable;
            throw throwable;
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            int status = response.getStatus();
            if (requestError != null && status < HttpServletResponse.SC_BAD_REQUEST) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }

            if (requestError == null) {
                log.info("http.request.completed method={} path={} status={} durationMs={}",
                        method, path, status, durationMs);
            } else {
                log.error("http.request.failed method={} path={} status={} durationMs={} errorType={} errorMessage={}",
                        method,
                        path,
                        status,
                        durationMs,
                        requestError.getClass().getSimpleName(),
                        requestError.getMessage());
            }
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incomingTraceId = request.getHeader(CORRELATION_ID_HEADER);
        if (incomingTraceId == null || incomingTraceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return incomingTraceId.trim();
    }

    private String buildPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + queryString;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}

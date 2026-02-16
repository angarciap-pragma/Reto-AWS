package com.aws.practice_aws.infrastructure.in.rest.error;

import com.aws.practice_aws.infrastructure.config.properties.ApiProperties;
import com.aws.practice_aws.shared.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiProperties apiProperties;

    public GlobalExceptionHandler(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException exception, HttpServletRequest request) {
        log.warn("http.request.business_error code={} status={} path={} message={}",
                exception.getCode(),
                exception.getStatus().value(),
                request.getRequestURI(),
                exception.getMessage());
        return buildResponse(
                exception.getStatus(),
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                             HttpServletRequest request) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationMessage)
                .toList();
        log.warn("http.request.validation_error status={} path={} detailsCount={}",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                details.size());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                apiProperties.errors().validationCode(),
                apiProperties.errors().validationMessage(),
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(HandlerMethodValidationException exception,
                                                                   HttpServletRequest request) {
        List<String> details = exception.getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage() == null ? error.toString() : error.getDefaultMessage())
                .toList();
        log.warn("http.request.validation_error status={} path={} detailsCount={}",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                details.size());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                apiProperties.errors().validationCode(),
                apiProperties.errors().validationMessage(),
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception,
                                                                      HttpServletRequest request) {
        List<String> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .toList();
        log.warn("http.request.validation_error status={} path={} detailsCount={}",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                details.size());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                apiProperties.errors().validationCode(),
                apiProperties.errors().validationMessage(),
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(NoHandlerFoundException exception,
                                                                 HttpServletRequest request) {
        log.warn("http.request.route_not_found status={} path={} method={}",
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                request.getMethod());
        return buildResponse(
                HttpStatus.NOT_FOUND,
                apiProperties.errors().notFoundCode(),
                apiProperties.errors().notFoundMessage(),
                request.getRequestURI(),
                List.of("No existe un endpoint para la ruta solicitada")
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception,
                                                                     HttpServletRequest request) {
        String supportedMethods = exception.getSupportedHttpMethods() == null
                ? "No informado"
                : exception.getSupportedHttpMethods().toString();
        log.warn("http.request.method_not_allowed status={} path={} method={} supported={}",
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                request.getRequestURI(),
                request.getMethod(),
                supportedMethods);
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                apiProperties.errors().methodNotAllowedCode(),
                apiProperties.errors().methodNotAllowedMessage(),
                request.getRequestURI(),
                List.of("Metodos permitidos: " + supportedMethods)
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException exception,
                                                                       HttpServletRequest request) {
        String supportedMediaTypes = exception.getSupportedMediaTypes().isEmpty()
                ? "No informado"
                : exception.getSupportedMediaTypes().toString();
        log.warn("http.request.unsupported_media_type status={} path={} contentType={} supported={}",
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                request.getRequestURI(),
                exception.getContentType(),
                supportedMediaTypes);
        return buildResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                apiProperties.errors().unsupportedMediaTypeCode(),
                apiProperties.errors().unsupportedMediaTypeMessage(),
                request.getRequestURI(),
                List.of("Tipos soportados: " + supportedMediaTypes)
        );
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Void> handleNotAcceptable(HttpMediaTypeNotAcceptableException exception,
                                                    HttpServletRequest request) {
        String supportedMediaTypes = exception.getSupportedMediaTypes().isEmpty()
                ? "No informado"
                : exception.getSupportedMediaTypes().toString();
        log.warn("http.request.not_acceptable status={} path={} accept={} supported={}",
                HttpStatus.NOT_ACCEPTABLE.value(),
                request.getRequestURI(),
                request.getHeader("Accept"),
                supportedMediaTypes);
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .header("Accept", supportedMediaTypes)
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("http.request.unexpected_error status={} path={} message={}",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                exception.getMessage(),
                exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                apiProperties.errors().unexpectedCode(),
                apiProperties.errors().unexpectedMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String code, String message, String path,
                                                           List<String> details) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(body);
    }

    private String toValidationMessage(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}

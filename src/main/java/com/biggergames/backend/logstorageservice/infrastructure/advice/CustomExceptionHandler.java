package com.biggergames.backend.logstorageservice.infrastructure.advice;

import com.biggergames.backend.logstorageservice.domain.common.ResponseCode;
import com.biggergames.backend.logstorageservice.domain.exception.BaseException;
import com.biggergames.backend.logstorageservice.infrastructure.response.ErrorResponseDto;
import com.biggergames.backend.logstorageservice.service.ResponseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@ControllerAdvice
public class CustomExceptionHandler {
    public static final String LOG_REQUEST_PLACEHOLDER = " Request: {}";
    public static final String LOG_MESSAGE_PLACEHOLDER = "error stack trace is";
    private final ResponseService responseService;

    // catches and logs RunTimeExceptions and returns ErrorResponse
    @ExceptionHandler(value = {BaseException.class})
    public ResponseEntity<ErrorResponseDto> handleCustomExceptions(BaseException e) {
        log.warn(e.getMessage() + LOG_REQUEST_PLACEHOLDER, getCurrentRequestMethodAndURL());
        log.error(LOG_MESSAGE_PLACEHOLDER, e);
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .message(e.getMessage())
                .type(e.getClass().getSimpleName())
                .build();
        return responseService.getErroneousResponseEntity(errorResponse, ResponseCode.SYSTEM_EXCEPTION);
    }

    @ExceptionHandler(value = {NoSuchKeyException.class})
    public ResponseEntity<ErrorResponseDto> handleNoSuchKeyException(Exception e) {
        log.warn(e.getMessage() + LOG_REQUEST_PLACEHOLDER, getCurrentRequestMethodAndURL());
        log.error(LOG_MESSAGE_PLACEHOLDER, e);
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .message(e.getMessage())
                .type(e.getClass().getSimpleName())
                .build();
        return responseService.getErroneousResponseEntity(errorResponse, ResponseCode.NOT_FOUND);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(Exception e) {
        log.warn(e.getMessage() + LOG_REQUEST_PLACEHOLDER, getCurrentRequestMethodAndURL());
        log.error(LOG_MESSAGE_PLACEHOLDER, e);
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .message(e.getMessage())
                .type(e.getClass().getSimpleName())
                .build();
        return responseService.getErroneousResponseEntity(errorResponse, ResponseCode.BAD_REQUEST);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
        log.warn(e.getMessage() + LOG_REQUEST_PLACEHOLDER, getCurrentRequestMethodAndURL());
        log.error(LOG_MESSAGE_PLACEHOLDER, e);
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .message(e.getMessage())
                .type(e.getClass().getSimpleName())
                .build();
        return responseService.getErroneousResponseEntity(errorResponse, ResponseCode.SYSTEM_EXCEPTION);
    }

    private String getCurrentRequestMethodAndURL() {
        return getCurrentHttpServletRequest()
                .map(httpServletRequest -> httpServletRequest.getMethod() + ":" + httpServletRequest.getRequestURL())
                .orElse(Strings.EMPTY);
    }

    private Optional<HttpServletRequest> getCurrentHttpServletRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }
}
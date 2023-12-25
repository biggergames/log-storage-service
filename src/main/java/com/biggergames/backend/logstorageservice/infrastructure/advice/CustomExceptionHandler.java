package com.biggergames.backend.logstorageservice.infrastructure.advice;

import com.biggergames.backend.logstorageservice.infrastructure.response.ErrorResponseDto;
import com.biggergames.backend.logstorageservice.service.ResponseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@ControllerAdvice
public class CustomExceptionHandler {
    public static final String LOG_REQUEST_PLACEHOLDER = " Request: {}";
    private final ResponseService responseService;

    // catches and logs RunTimeExceptions and returns ErrorResponse
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorResponseDto> handle(Exception e) {
        log.warn(e.getMessage() + LOG_REQUEST_PLACEHOLDER, getCurrentRequestMethodAndURL());
        log.error("error stack trace is", e);
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .message(e.getMessage())
                .type(e.getClass().getSimpleName())
                .build();
        return responseService.getErroneousResponseEntity(errorResponse);
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
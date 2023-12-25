package com.biggergames.backend.logstorageservice.service;

import com.biggergames.backend.logstorageservice.config.ApplicationConfig;
import com.biggergames.backend.logstorageservice.domain.common.ResponseCode;
import com.biggergames.backend.logstorageservice.infrastructure.response.BaseResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.biggergames.backend.logstorageservice.domain.common.CustomHeader.*;

@Service
@AllArgsConstructor
@Slf4j
public class ResponseService {
    private final ApplicationConfig applicationConfig;

    public <T extends BaseResponseDto> ResponseEntity<T> getResponseEntity(HttpServletRequest request, T body) {
        return getResponseEntity(request, body, ResponseCode.SUCCESS);
    }

    // generates response entity for list objects
    public <T extends BaseResponseDto> ResponseEntity<List<T>> getResponseEntity(HttpServletRequest request, List<T> body) {
        return ResponseEntity.status(HttpStatus.OK)
                .header(BG_CODE, ResponseCode.SUCCESS.getCode())
                .header(BG_TIME, time())
                .header(BG_TYPE, ResponseCode.SUCCESS.getType())
                .header(BG_TRANSACTION_ID, getTransactionId(request))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*")
                .body(body);
    }

    public <T extends BaseResponseDto> ResponseEntity<T> getErroneousResponseEntity(T errorResponse) {
        return getResponseEntity(null, errorResponse, ResponseCode.SYSTEM_EXCEPTION);
    }

    private <T extends BaseResponseDto> ResponseEntity<T> getResponseEntity(HttpServletRequest request, T body, ResponseCode responseCode) {
        return ResponseEntity.status(HttpStatus.OK)
                .header(BG_CODE, responseCode.getCode())
                .header(BG_TIME, time())
                .header(BG_TYPE, responseCode.getType())
                .header(BG_TRANSACTION_ID, getTransactionId(request))
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*")
                .body(body);
    }

    private String time() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern(applicationConfig.getBgTimeFormat()));
    }

    private static String getTransactionId(HttpServletRequest request) {
        if (request == null) {
            return "empty-transaction-id";
        }
        String transactionId = request.getHeader(BG_TRANSACTION_ID);
        if (ObjectUtils.isEmpty(transactionId)) {
            transactionId = (String) request.getAttribute(BG_TRANSACTION_ID);
        }
        return transactionId;
    }
}
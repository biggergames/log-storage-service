package com.biggergames.backend.logstorageservice.domain.exception;

import com.biggergames.backend.logstorageservice.domain.common.ResponseCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ResponseCode responseCode;

    public BaseException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }
}
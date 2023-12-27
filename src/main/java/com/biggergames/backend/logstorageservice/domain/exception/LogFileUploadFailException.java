package com.biggergames.backend.logstorageservice.domain.exception;

import com.biggergames.backend.logstorageservice.domain.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogFileUploadFailException extends BaseException {
    public LogFileUploadFailException(String message) {
        super(ResponseCode.SYSTEM_EXCEPTION, message);
        log.error(message);
    }
}

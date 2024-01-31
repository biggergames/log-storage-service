package com.biggergames.backend.logstorageservice.infrastructure.controller;

import com.biggergames.backend.logstorageservice.domain.common.ResponseCode;
import com.biggergames.backend.logstorageservice.infrastructure.response.BaseResponseDto;
import com.biggergames.backend.logstorageservice.infrastructure.response.LoadLogFileResponseDto;
import com.biggergames.backend.logstorageservice.service.LogStorageService;
import com.biggergames.backend.logstorageservice.service.ResponseService;
import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.biggergames.backend.logstorageservice.domain.common.ApiConstants.API_PREFIX;

@Validated
@RestController
@RequestMapping(API_PREFIX)
@RequiredArgsConstructor
@Slf4j
public class LogStorageController {
    private final LogStorageService logStorageService;
    private final ResponseService responseService;

//    @PostMapping(value = "/save", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    @Timed(value = "lss.controller.storage.save")
//    public ResponseEntity<BaseResponseDto> save(@RequestParam @NotEmpty String accountId,
//                                                @RequestPart("files") MultipartFile[] logFiles,
//                                                HttpServletRequest request) throws IOException {
//        log.debug("save request arrived for accountId: {}, logFiles: {}", accountId, getFileNames(logFiles));
//
//        logStorageService.saveLogFiles(accountId, logFiles);
//        return responseService.getResponseEntity(request, new BaseResponseDto("Files are uploaded successfully"));
//    }

    @PostMapping(value = "/save", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Timed(value = "lss.controller.storage.save")
    public ResponseEntity<BaseResponseDto> saveZip(@RequestParam @NotEmpty String accountId,
                                                @RequestPart("files") MultipartFile zipLogFile,
                                                HttpServletRequest request) throws IOException {
        log.debug("save request arrived for accountId: {}, logFiles: {}", accountId, zipLogFile);

        logStorageService.saveZipLogFile(accountId, zipLogFile);
        return responseService.getResponseEntity(request, new BaseResponseDto("Files are uploaded successfully"));
    }

    @GetMapping(value = "/load", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin
    @Timed(value = "lss.controller.storage.load")
    public ResponseEntity<LoadLogFileResponseDto> load(@RequestParam @NotEmpty String key,
                                                       HttpServletRequest request) {
        log.debug("load request arrived for key: {}", key);

        LoadLogFileResponseDto logFile = logStorageService.getLogFile(key);
        return responseService.getResponseEntity(request, logFile);
    }

    @GetMapping(value = "/load-list", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin
    @Timed(value = "lss.controller.storage.load-list")
    public ResponseEntity<LoadLogFileResponseDto> loadList(@RequestParam @NotEmpty String accountId,
                                                           HttpServletRequest request) {
        log.debug("load-list request arrived for accountId: {}", accountId);

        LoadLogFileResponseDto logFile = logStorageService.getLogFileList(accountId);
        return responseService.getResponseEntity(request, logFile);
    }

    @GetMapping(value = "/download-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @CrossOrigin
    @Timed(value = "lss.controller.storage.download-all")
    public ResponseEntity<Resource> downloadAll(@RequestParam @NotEmpty String accountId,
                                         @RequestParam @NotEmpty String directoryKey,
                                         HttpServletRequest request) {
        log.debug("download-all request arrived for accountId: {}", accountId);
        Resource resource = logStorageService.downloadAll(accountId, directoryKey);
        if (resource == null) {
            return responseService.getErroneousResponseEntity(request, ResponseCode.NOT_FOUND);
        }
        return responseService.getResponseEntity(request, resource);
    }

    private List<String> getFileNames(MultipartFile[] multipartFiles) {
        return Arrays.stream(multipartFiles)
                .map(m -> m.getOriginalFilename() == null ? "empty_file" : m.getOriginalFilename()).toList();
    }
}

package com.biggergames.backend.logstorageservice.service;

import com.biggergames.backend.logstorageservice.config.S3Config;
import com.biggergames.backend.logstorageservice.domain.exception.LogFileUploadFailException;
import com.biggergames.backend.logstorageservice.infrastructure.response.LoadLogFileResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class LogStorageService {
    public static final String S3_KEY_FIELD = "Key";
    private final S3Config s3Config;
    private final S3Client s3Client;

    private static final String FILE_DELIMITER = "/";

    public void saveLogFiles(String accountId, MultipartFile[] logFiles) throws IOException {
        // checks if multipart file array is valid
        if (!isLogFileArrayValid(logFiles)) {
            throw new LogFileUploadFailException(String.format("Multipart file array is not valid, accountId: %s,", accountId));
        }
        // gets the save date for directory name with formatted now date 'yyyyMMddHHmmss'
        String saveDate = getSaveDate();
        // iterates over log files
        for (MultipartFile logFile : logFiles) {
            // checks if the log file is valid,
            if (!isLogFileValid(logFile)) {
                // if not, continue iterating because the one broken file should not halt the whole process.
                log.error("Log file is not valid, fileName: {}, file size: {}, accountId: {}",
                        logFile.getOriginalFilename(), logFile.getSize(), accountId);
                continue;
            }

            // creates temp file save direction
            Path tempFile = Files.createTempFile("s3-upload-", ".tmp");
            logFile.transferTo(new File(tempFile.toString()));

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(s3Config.getKey()
                            .concat(accountId)
                            .concat(FILE_DELIMITER)
                            .concat(saveDate)
                            .concat(FILE_DELIMITER)
                            .concat(Objects.requireNonNull(logFile.getOriginalFilename())))
                    .build();

            s3Client.putObject(request, RequestBody.fromFile(tempFile.toFile()));
        }
    }

    // gets exact file
    public LoadLogFileResponseDto getLogFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(key)
                .build();
        ResponseBytes<GetObjectResponse> objectResponseAsBytes = s3Client.getObjectAsBytes(getObjectRequest);
        String fileContent = new String(objectResponseAsBytes.asByteArray(), StandardCharsets.UTF_8);

        return new LoadLogFileResponseDto(key, fileContent, null);
    }

    // gets file name list
    public LoadLogFileResponseDto getLogFileList(String accountId) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(s3Config.getBucketName()).prefix(s3Config.getKey().concat(accountId)).build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
        List<S3Object> s3ObjectList = listObjectsV2Response.contents();

        // gets file names into a string list
        List<String> keys = new ArrayList<>();
        for (S3Object logFileS3Object : s3ObjectList) {
            Optional<String> optionalFileName = logFileS3Object.getValueForField(S3_KEY_FIELD, String.class);
            optionalFileName.ifPresent(keys::add);
        }

        return new LoadLogFileResponseDto(null, null, keys);
    }

    // checks if a single log file is valid
    private boolean isLogFileValid(MultipartFile logFile) {
        return !(logFile == null
                || logFile.isEmpty()
                || Objects.equals("", logFile.getOriginalFilename())
                || Objects.equals(null, logFile.getOriginalFilename()));
    }

    // checks if log file array is valid
    private boolean isLogFileArrayValid(MultipartFile[] logFiles) {
        return logFiles != null && logFiles.length > 0;
    }

    private String getSaveDate() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern(s3Config.getSaveDateFormat()));
    }
}
package com.biggergames.backend.logstorageservice.service;

import com.biggergames.backend.logstorageservice.config.S3Config;
import com.biggergames.backend.logstorageservice.domain.exception.LogFileUploadFailException;
import com.biggergames.backend.logstorageservice.infrastructure.response.LoadLogFileResponseDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class LogStorageService {
    private final S3Config s3Config;
    private final S3Client s3Client;

    private static final String FILE_DELIMITER = "/";

    public void saveLogFiles(String accountId, MultipartFile[] logFiles) throws IOException {
        // checks if multipart file array is valid
        if (isLogFileArrayValid(logFiles)) {
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

    public LoadLogFileResponseDto getLogFile(String path) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(s3Config.getKey().concat(path))
                .build();

        ResponseBytes<GetObjectResponse> objectResponseAsBytes = s3Client.getObjectAsBytes(getObjectRequest);
        String fileContent = new String(objectResponseAsBytes.asByteArray(), StandardCharsets.UTF_8);

        return new LoadLogFileResponseDto(fileContent, path);
    }

    // checks if a single log file is valid
    private boolean isLogFileValid(MultipartFile logFile) {
        return !(logFile == null
                || logFile.isEmpty()
                || Objects.equals("", logFile.getOriginalFilename())
                || Objects.equals(null, logFile.getOriginalFilename())
                || logFile.getSize() > s3Config.getMaxFileSize());
    }

    // checks if log file array is valid
    private boolean isLogFileArrayValid(MultipartFile[] logFiles) {
        return logFiles != null && logFiles.length > 0;
    }

    private String getSaveDate() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern(s3Config.getSaveDateFormat()));
    }
}
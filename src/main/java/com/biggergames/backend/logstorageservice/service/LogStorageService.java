package com.biggergames.backend.logstorageservice.service;

import com.biggergames.backend.logstorageservice.config.S3Config;
import com.biggergames.backend.logstorageservice.domain.exception.LogFileUploadFailException;
import com.biggergames.backend.logstorageservice.infrastructure.response.LoadLogFileResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.Files;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.mock.web.MockMultipartFile;


@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class LogStorageService {
    public static final String S3_KEY_FIELD = "Key";
    private final S3Config s3Config;
    private final S3Client s3Client;

    private static final String DIRECTORY_DELIMITER = "/";

    public void saveZipLogFile(String accountId, MultipartFile zipLogFile) throws IOException {
        if (zipLogFile == null ) {
            throw new LogFileUploadFailException(String.format("Multipart zip file is null, accountId: %s,", accountId));
        }
        List<MultipartFile> multipartFiles = extractZipFile(zipLogFile);
        if (CollectionUtils.isEmpty(multipartFiles)) {
            throw new LogFileUploadFailException(String.format("Extracted multipart files is empty, accountId: %s,", accountId));
        }
        saveLogFiles(accountId, multipartFiles.toArray(new MultipartFile[0]));
    }

    private void saveLogFiles(String accountId, MultipartFile[] logFiles) throws IOException {
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
                            .concat(DIRECTORY_DELIMITER)
                            .concat(saveDate)
                            .concat(DIRECTORY_DELIMITER)
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

    // downloads all files from a directory as zip
    public Resource downloadAll(String accountId, String directoryName) {
        // lists all objects from given directory key
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(s3Config.getBucketName())
                .prefix(s3Config.getKey()
                        .concat(accountId)
                        .concat(DIRECTORY_DELIMITER)
                        .concat(directoryName))
                .build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
        // checks if object list is valid
        if (listObjectsV2Response == null || listObjectsV2Response.contents().isEmpty()) {
            log.error("Logs do not exist for given accountId: {} and directory key: {}", accountId, directoryName);
            return null;
        }

        // creates a ZipOutputStream to write files into
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            // for each object in directory
            for (S3Object s3Object : listObjectsV2Response.contents()) {
                // crates a zip entry object
                ZipEntry zipEntry = new ZipEntry(s3Object.key());
                zipOutputStream.putNextEntry(zipEntry);
                // makes a request to fetch file content from s3
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(s3Config.getBucketName())
                        .key(s3Object.key())
                        .build();
                ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest);
                if (responseInputStream == null || responseInputStream.response() == null) {
                    log.error("The log file from given directory is null, accountId: {}, key:{}", accountId, s3Object.key());
                    continue;
                }

                // writes content to zipOutputStream as bytes
                zipOutputStream.write(responseInputStream.readAllBytes());
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            log.error("Log files could not be downloaded, accountId: {}, directory: {}", accountId, directoryName);
        }
        return new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
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

    public List<MultipartFile> extractZipFile(MultipartFile zipLogFile) throws IOException {
        List<MultipartFile> extractedFiles = new ArrayList<>();

        try (InputStream inputStream = zipLogFile.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    byte[] fileBytes = zipInputStream.readAllBytes();
                    String fileName = zipEntry.getName();
                    MultipartFile extractedFile = new MockMultipartFile(fileName, fileName, "text/plain", fileBytes);
                    extractedFiles.add(extractedFile);
                }
            }
        }

        return extractedFiles;
    }
}
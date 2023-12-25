package com.biggergames.backend.logstorageservice.infrastructure.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LoadLogFileResponseDto extends BaseResponseDto {
    private String fileName;
    private String fileContent;
}

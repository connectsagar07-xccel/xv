package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelyReportAttachment {

    private String fileName;  // The actual name of the file
    private String filePath;  // Full absolute path on disk
}

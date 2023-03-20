package com.test.extracts.config.properties;

import lombok.Data;

@Data
public class BatchConfiguration {
    private Integer chunkSize = 10;
    private Integer pageSize = 10;
    private Integer maxItemCount = 10;
    private Integer itemCountLimitPerResource = 10;
    private String fileNamePrefix = "defaultPrefix";
    private String outputDir = "output";
    private String delimiter = ",";
    private QueryConfig queryConfig;
}

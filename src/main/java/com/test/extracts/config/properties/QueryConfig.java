package com.test.extracts.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class QueryConfig {
    @NotBlank
    private String selectClause;
    @NotBlank
    private String fromClause;
    @NotBlank
    private String sortKey;
    @NotEmpty
    private String[] fields;
}

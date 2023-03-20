package com.test.extracts.batch.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Purchase {

    private String id;
    private String name;
}

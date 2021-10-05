package com.projects.onlinejudge.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestCaseDTO {

    private long id;
    private String problemCode;
    private Boolean isSampleTest = false;
}

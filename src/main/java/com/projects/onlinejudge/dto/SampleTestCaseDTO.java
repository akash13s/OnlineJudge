package com.projects.onlinejudge.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleTestCaseDTO extends TestCaseDTO{

    private String sampleInput;
    private String sampleOutput;
}

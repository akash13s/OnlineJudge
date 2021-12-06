package com.projects.onlinejudge.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class ProblemDTO {

    private Long id;

    @NotBlank(message = "problemCode cannot be blank")
    private String problemCode;

    @NotBlank(message = "userName cannot be blank")
    private String userName;

    @NotBlank(message = "title cannot be blank")
    private String title;

    @NotBlank(message = "description cannot be blank")
    private String description;

    @NotBlank(message = "inputFormat cannot be blank")
    private String inputFormat;

    @NotBlank(message = "outputFormat cannot be blank")
    private String outputFormat;

    @NotBlank(message = "constraints cannot be blank")
    private String constraints;

    private List<SampleTestCaseDTO> testCases;

}

package com.projects.onlinejudge.dto;

import com.projects.onlinejudge.domain.Problem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContestDTO {

    private Long id;
    private String contestName;
    private Date startDate;
    private Date endDate;
    private String description;
    private String rules;
    private String scoring;

    private List<Problem> problems = new ArrayList<>();

}

package com.projects.onlinejudge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContestRegistrationDTO {

    private String userName;
    private Long contestId;
}

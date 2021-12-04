package com.projects.onlinejudge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProblemDTO {

    public String problemCode;
    public Boolean problemSolved;
    public Date time;
    public Integer tryCount;
}

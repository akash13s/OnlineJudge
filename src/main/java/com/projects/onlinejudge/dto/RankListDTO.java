package com.projects.onlinejudge.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RankListDTO {

    public String userName;
    public List<UserProblemDTO> userProblemData;
}

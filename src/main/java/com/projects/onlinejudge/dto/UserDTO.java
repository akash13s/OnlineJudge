package com.projects.onlinejudge.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {

    private String userName;
    private String email;
    private String password;
    private String confirmPassword;

}

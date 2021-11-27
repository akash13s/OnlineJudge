package com.projects.onlinejudge.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class User {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    private String userName;
    private String email;
    private String password;
    private String confirmPassword;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Problem> userProblems = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Contest> contests = new ArrayList<>();

}

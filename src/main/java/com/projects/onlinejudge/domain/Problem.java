package com.projects.onlinejudge.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String problemCode;
    private String userName;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "problem", orphanRemoval = true)
    private List<TestCase> testCases = new ArrayList<>();

}

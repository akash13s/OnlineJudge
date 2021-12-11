package com.projects.onlinejudge.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
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

    private Long numberOfTestCases = 0L;
    private Long nextTestCaseId = 1L;

    private Date createdAt;
    private Date updatedAt;

    private Date testCaseLastUpdatedAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "problem", orphanRemoval = true)
    private List<TestCase> testCases = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Contest contest;

}

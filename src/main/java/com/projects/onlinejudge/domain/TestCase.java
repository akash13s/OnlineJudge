package com.projects.onlinejudge.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idx;
    // id of problem within Problem folder
    private Long id;
    private String problemCode;

    @ManyToOne(fetch = FetchType.EAGER)
    private Problem problem;

    private String inputFileName;
    private String outputFileName;
    private boolean isSampleTest;
    private String sampleInput;
    private String sampleOutput;

}

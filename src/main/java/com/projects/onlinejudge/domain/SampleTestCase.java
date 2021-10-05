package com.projects.onlinejudge.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class SampleTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idx;
    private Long id;
    private String problemCode;

    @ManyToOne(fetch = FetchType.EAGER)
    private Problem problem;

    private String sampleInput;
    private String sampleOutput;

}

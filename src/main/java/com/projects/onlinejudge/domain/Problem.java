package com.projects.onlinejudge.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
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

    private Date createdAt;
    private Date updatedAt;
}

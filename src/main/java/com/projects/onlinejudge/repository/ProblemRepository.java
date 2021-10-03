package com.projects.onlinejudge.repository;

import com.projects.onlinejudge.domain.Problem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends CrudRepository<Problem, Long> {

    Problem findProblemByProblemCode(String problemCode);
}

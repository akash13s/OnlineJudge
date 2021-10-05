package com.projects.onlinejudge.repository;

import com.projects.onlinejudge.domain.Problem;
import com.projects.onlinejudge.domain.SampleTestCase;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleTestCaseRepository extends CrudRepository<SampleTestCase, Long> {

    Iterable<SampleTestCase> findAllByProblemCode(String problemCode);

    SampleTestCase findByProblemAndId(Problem problem, Long id);
}

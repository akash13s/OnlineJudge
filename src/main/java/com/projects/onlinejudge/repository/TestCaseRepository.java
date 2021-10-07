package com.projects.onlinejudge.repository;

import com.projects.onlinejudge.domain.Problem;
import com.projects.onlinejudge.domain.TestCase;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends CrudRepository<TestCase, Long> {

    Iterable<TestCase> findAllByProblemCode(String problemCode);

    TestCase findByProblemAndId(Problem problem, Long id);
}

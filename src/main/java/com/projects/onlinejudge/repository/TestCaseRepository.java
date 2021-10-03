package com.projects.onlinejudge.repository;

import com.projects.onlinejudge.domain.TestCase;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends CrudRepository<TestCase, Long> {

}

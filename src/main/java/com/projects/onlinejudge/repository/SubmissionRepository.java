package com.projects.onlinejudge.repository;

import com.projects.onlinejudge.domain.Submission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends CrudRepository<Submission, Long> {

}

package com.projects.onlinejudge.repository;

import com.projects.onlinejudge.domain.Contest;
import org.springframework.data.repository.CrudRepository;

public interface ContestRepository extends CrudRepository<Contest, Long> {

    Contest findByContestName(String contestName);
}

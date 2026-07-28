package com.tab.tab.repository;

import com.tab.tab.entity.Contingent;
import com.tab.tab.entity.Criteria;
import com.tab.tab.entity.Judge;
import com.tab.tab.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByContingentAndCriteria(Contingent contingent, Criteria criteria);
    Optional<Score> findByJudgeAndContingentAndCriteria(Judge judge, Contingent contingent, Criteria criteria);
}
package com.tab.tab.service;

import com.tab.tab.entity.Score;
import com.tab.tab.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Transactional
    public List<Score> saveAllScores(List<Score> scoresToSave) {
        List<Score> savedScores = new ArrayList<>();
        for (Score score : scoresToSave) {
            Optional<Score> existingScoreOpt = scoreRepository.findByJudgeAndContingentAndCriteria(
                    score.getJudge(), score.getContingent(), score.getCriteria());

            Score scoreToSave;
            if (existingScoreOpt.isPresent()) {
                scoreToSave = existingScoreOpt.get();
                scoreToSave.setRawScore(score.getRawScore());
            } else {
                scoreToSave = score;
            }
            savedScores.add(scoreRepository.save(scoreToSave));
        }
        return savedScores;
    }
}

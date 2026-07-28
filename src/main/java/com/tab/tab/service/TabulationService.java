package com.tab.tab.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tab.tab.entity.Category;
import com.tab.tab.entity.Contingent;
import com.tab.tab.entity.Criteria;
import com.tab.tab.entity.Score;
import com.tab.tab.entity.dto.ContingentResultDTO;
import com.tab.tab.repository.ContingentRepository;
import com.tab.tab.repository.CriteriaRepository;
import com.tab.tab.repository.ScoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TabulationService {

    private final ScoreRepository scoreRepository;
    private final ContingentRepository contingentRepository;
    private final CriteriaRepository criteriaRepository;

    public List<ContingentResultDTO> calculateAllContingentResults() {
        List<ContingentResultDTO> results = contingentRepository.findAll().stream()
                .map(this::calculateContingentResult)
                .sorted((r1, r2) -> Double.compare(r2.getGrandTotal(), r1.getGrandTotal()))
                .collect(Collectors.toList());

        // Assign ranks, handling ties
        if (!results.isEmpty()) {
            int rank = 1;
            results.get(0).setRank(rank);
            for (int i = 1; i < results.size(); i++) {
                if (results.get(i).getGrandTotal() < results.get(i - 1).getGrandTotal()) {
                    rank = i + 1;
                }
                results.get(i).setRank(rank);
            }
        }

        return results;
    }

    public ContingentResultDTO calculateContingentResult(Contingent contingent) {
        ContingentResultDTO resultDTO = new ContingentResultDTO();
        resultDTO.setContingentName(contingent.getDisplayName());

        Map<Criteria, Double> weightedScores = new HashMap<>();

        for (Criteria criteria : criteriaRepository.findAll()) {
            List<Score> scores = scoreRepository.findByContingentAndCriteria(contingent, criteria);
            double averageRawScore = scores.stream()
                    .mapToInt(Score::getRawScore)
                    .average()
                    .orElse(0.0);

            double weightedScore = averageRawScore * criteria.getWeight();
            weightedScores.put(criteria, weightedScore);
        }

        resultDTO.setWeightedScores(weightedScores);

        double streetDanceTotal = weightedScores.entrySet().stream().filter(e -> e.getKey().getCategory() == Category.STREET_DANCE).mapToDouble(Map.Entry::getValue).sum();
        double culturalShowdownTotal = weightedScores.entrySet().stream().filter(e -> e.getKey().getCategory() == Category.CULTURAL_SHOWDOWN).mapToDouble(Map.Entry::getValue).sum();

        resultDTO.setStreetDanceTotal(streetDanceTotal);
        resultDTO.setCulturalShowdownTotal(culturalShowdownTotal);
        resultDTO.setGrandTotal(streetDanceTotal + culturalShowdownTotal);

        return resultDTO;
    }
}
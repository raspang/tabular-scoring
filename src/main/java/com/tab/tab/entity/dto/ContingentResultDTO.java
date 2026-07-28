package com.tab.tab.entity.dto;

import java.util.Map;

import com.tab.tab.entity.Criteria;

import lombok.Data;

@Data
public class ContingentResultDTO {
    private String contingentName;
    private Map<Criteria, Double> weightedScores;
    private double streetDanceTotal;
    private double culturalShowdownTotal;
    private double grandTotal;
    private int rank;
}
package com.tab.tab.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(
    name = "score"
    //, uniqueConstraints = {
    //     @UniqueConstraint(
    //         name = "uk_judge_contingent_criteria",
    //         columnNames = {"judge_id", "contingent_id", "criteria_id"}
    //     )
    //}
)
@AllArgsConstructor
@NoArgsConstructor
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "judge_id")
    private Judge judge;

    @ManyToOne
    @JoinColumn(name = "contingent_id")
    private Contingent contingent;

    @ManyToOne
    @JoinColumn(name = "criteria_id")
    private Criteria criteria;

    @Min(1)
    @Max(10)
    private Integer rawScore;
}

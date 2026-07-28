package com.tab.tab.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "criteria", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"displayName", "category"})
})
@NoArgsConstructor
public class Criteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;

    @Enumerated(EnumType.STRING)
    private Category category;

    private double weight;

    public Criteria(String displayName, Category category, double weight) {
        this.displayName = displayName;
        this.category = category;
        this.weight = weight;
    }
}

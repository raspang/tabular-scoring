package com.tab.tab.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "contingent", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"displayName"})
})
@NoArgsConstructor
public class Contingent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;

    public Contingent(String displayName) {
        this.displayName = displayName;
    }
}

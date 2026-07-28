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
@Table(name = "judge", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"})
})
@NoArgsConstructor
public class Judge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Judge(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }

}

package com.erokin.campusclubmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "interest_tags")
public class InterestTag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String name;
}


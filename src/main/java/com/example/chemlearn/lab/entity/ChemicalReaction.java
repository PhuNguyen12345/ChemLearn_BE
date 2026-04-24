package com.example.chemlearn.lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "chemical_reaction")
public class ChemicalReaction {
    @Id
    @Column(name = "reaction_key", nullable = false, length = 100)
    private String reactionKey;

    @Column(name = "liquid_content", length = 100)
    private String liquidContent;

    @Column(name = "solid_content", length = 100)
    private String solidContent;

    @Column(name = "gas_content", length = 100)
    private String gasContent;

    @Column(name = "liquid_color", length = 50)
    private String liquidColor;

    @Column(name = "precipitate_color", length = 50)
    private String precipitateColor;

    @Column(name = "reaction_state", length = 50)
    private String reactionState;

    @Column(name = "reaction_info")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> reactionInfo;

}
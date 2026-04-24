package com.example.chemlearn.lab.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lab_configuration")
public class LabConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @Column(name = "config")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> config;

    @Column(name = "viewport")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> viewport;

    @Column(name = "initial_workspace")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> initialWorkspace;

}
package com.example.chemlearn.lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventory_item")
public class InventoryItem {
    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "state", length = 20)
    private String state;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "icon_data")
    private String iconData;

    @Column(name = "properties")
    private String properties;

}
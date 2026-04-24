package com.example.chemlearn.lab.entity;

import com.example.chemlearn.lab.enums.ItemType;
import com.example.chemlearn.lab.enums.PhysicalState;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private ItemType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20)
    private PhysicalState state;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "icon_data")
    private String iconData;

    @Column(name = "properties")
    private String properties;

}
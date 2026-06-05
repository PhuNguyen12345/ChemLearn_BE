package com.example.chemlearn.lab.entity;

import com.example.chemlearn.lab.enums.ItemType;
import com.example.chemlearn.lab.enums.PhysicalState;
import com.example.chemlearn.lab.enums.SubCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventory_item")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "item_code", nullable = false, unique = true, length = 50)
    private String itemCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private ItemType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", length = 50)
    private SubCategory subCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20)
    private PhysicalState state;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Column(name = "icon_color", length = 50)
    private String iconColor;

    @Column(name = "icon_fill", length = 50)
    private String iconFill;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "properties", columnDefinition = "JSONB")
    private String properties;
}
package com.example.chemlearn.gamification.repository;

import com.example.chemlearn.gamification.entity.Item;
import com.example.chemlearn.gamification.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    long countByItemType(ItemType itemType);
    List<Item> findByItemTypeOrderByNameAsc(ItemType itemType);
}

package com.example.chemlearn.lab.service.impl;

import com.example.chemlearn.lab.entity.InventoryItem;
import com.example.chemlearn.lab.repository.InventoryRepository;
import com.example.chemlearn.lab.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getAllInventoryItems() {
        return inventoryRepository.findAll();
    }
}

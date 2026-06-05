package com.example.chemlearn.lab.controller;

import com.example.chemlearn.lab.entity.InventoryItem;
import com.example.chemlearn.lab.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lab/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryItem>> getAllInventoryItems() {
        return ResponseEntity.ok(inventoryService.getAllInventoryItems());
    }
}

package com.example.chemlearn.gamification.controller;

import com.example.chemlearn.gamification.dto.admin.AdminEggDropRateDTO;
import com.example.chemlearn.gamification.dto.admin.AdminEggItemDTO;
import com.example.chemlearn.gamification.dto.admin.AdminPetSpeciesDTO;
import com.example.chemlearn.gamification.service.AdminPetManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/pets")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPetManagementController {
    private final AdminPetManagementService adminPetManagementService;

    @GetMapping("/species")
    public ResponseEntity<List<AdminPetSpeciesDTO>> getPetSpecies() {
        return ResponseEntity.ok(adminPetManagementService.getPetSpecies());
    }

    @PostMapping("/species")
    public ResponseEntity<AdminPetSpeciesDTO> createPetSpecies(@Valid @RequestBody AdminPetSpeciesDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminPetManagementService.createPetSpecies(dto));
    }

    @PutMapping("/species/{id}")
    public ResponseEntity<AdminPetSpeciesDTO> updatePetSpecies(@PathVariable UUID id,
                                                               @Valid @RequestBody AdminPetSpeciesDTO dto) {
        return ResponseEntity.ok(adminPetManagementService.updatePetSpecies(id, dto));
    }

    @DeleteMapping("/species/{id}")
    public ResponseEntity<Void> deletePetSpecies(@PathVariable UUID id) {
        adminPetManagementService.deletePetSpecies(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/eggs")
    public ResponseEntity<List<AdminEggItemDTO>> getEggItems() {
        return ResponseEntity.ok(adminPetManagementService.getEggItems());
    }

    @PostMapping("/eggs")
    public ResponseEntity<AdminEggItemDTO> createEggItem(@Valid @RequestBody AdminEggItemDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminPetManagementService.createEggItem(dto));
    }

    @PutMapping("/eggs/{id}")
    public ResponseEntity<AdminEggItemDTO> updateEggItem(@PathVariable UUID id,
                                                         @Valid @RequestBody AdminEggItemDTO dto) {
        return ResponseEntity.ok(adminPetManagementService.updateEggItem(id, dto));
    }

    @DeleteMapping("/eggs/{id}")
    public ResponseEntity<Void> deleteEggItem(@PathVariable UUID id) {
        adminPetManagementService.deleteEggItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/drop-rates")
    public ResponseEntity<List<AdminEggDropRateDTO>> getDropRates() {
        return ResponseEntity.ok(adminPetManagementService.getDropRates());
    }

    @PostMapping("/drop-rates")
    public ResponseEntity<AdminEggDropRateDTO> createDropRate(@Valid @RequestBody AdminEggDropRateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminPetManagementService.createDropRate(dto));
    }

    @PutMapping("/drop-rates/{id}")
    public ResponseEntity<AdminEggDropRateDTO> updateDropRate(@PathVariable UUID id,
                                                              @Valid @RequestBody AdminEggDropRateDTO dto) {
        return ResponseEntity.ok(adminPetManagementService.updateDropRate(id, dto));
    }

    @DeleteMapping("/drop-rates/{id}")
    public ResponseEntity<Void> deleteDropRate(@PathVariable UUID id) {
        adminPetManagementService.deleteDropRate(id);
        return ResponseEntity.noContent().build();
    }
}

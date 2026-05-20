package com.example.chemlearn.gamification.controller;

import com.example.chemlearn.gamification.dto.pet.GachaResultDTO;
import com.example.chemlearn.gamification.dto.pet.InventoryItemDTO;
import com.example.chemlearn.gamification.dto.pet.PetActionRequestDTO;
import com.example.chemlearn.gamification.dto.pet.StudentPetDTO;
import com.example.chemlearn.gamification.service.PetSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/pets")
@RequiredArgsConstructor
public class PetSystemController {

    private final PetSystemService petSystemService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudentPetDTO>> getMyPets(Authentication authentication) {
        return ResponseEntity.ok(petSystemService.getMyPets(authentication.getName()));
    }

    @GetMapping("/coins")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Integer> getMyCoins(Authentication authentication) {
        return ResponseEntity.ok(petSystemService.getCoins(authentication.getName()));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<InventoryItemDTO>> getMyInventory(Authentication authentication) {
        return ResponseEntity.ok(petSystemService.getMyInventory(authentication.getName()));
    }

    @GetMapping("/shop/items")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<InventoryItemDTO>> getAllShopItems() {
        return ResponseEntity.ok(petSystemService.getAllShopItems());
    }

    @PostMapping("/shop/buy")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> buyItem(Authentication authentication, @RequestBody PetActionRequestDTO request) {
        petSystemService.buyItem(authentication.getName(), request.getItemId(), request.getQuantity());
        return ResponseEntity.ok("Bought successfully");
    }

    @PostMapping("/gacha")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GachaResultDTO> openEgg(Authentication authentication, @RequestBody PetActionRequestDTO request) {
        return ResponseEntity.ok(petSystemService.openEgg(authentication.getName(), request.getItemId()));
    }

    @PostMapping("/{id}/feed")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> feedPet(Authentication authentication, @PathVariable UUID id, @RequestBody PetActionRequestDTO request) {
        petSystemService.feedPet(authentication.getName(), id, request.getItemId(), request.getQuantity());
        return ResponseEntity.ok("Pet fed successfully");
    }

    @PostMapping("/{id}/star-up")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> starUpPet(Authentication authentication, @PathVariable UUID id) {
        petSystemService.starUpPet(authentication.getName(), id);
        return ResponseEntity.ok("Pet star increased successfully");
    }
}

package com.example.chemlearn.gamification.service;

import com.example.chemlearn.gamification.dto.pet.GachaResultDTO;
import com.example.chemlearn.gamification.dto.pet.InventoryItemDTO;
import com.example.chemlearn.gamification.dto.pet.StudentPetDTO;

import java.util.List;
import java.util.UUID;

public interface PetSystemService {
    List<StudentPetDTO> getMyPets(String username);
    List<InventoryItemDTO> getMyInventory(String username);
    List<InventoryItemDTO> getAllShopItems();
    int getCoins(String username);
    
    void buyItem(String username, UUID itemId, int quantity);
    GachaResultDTO openEgg(String username, UUID eggItemId);
    void feedPet(String username, UUID studentPetId, UUID foodItemId, int quantity);
    void starUpPet(String username, UUID studentPetId);
}

package com.example.chemlearn.gamification.service;

import com.example.chemlearn.gamification.dto.admin.AdminEggDropRateDTO;
import com.example.chemlearn.gamification.dto.admin.AdminEggItemDTO;
import com.example.chemlearn.gamification.dto.admin.AdminPetSpeciesDTO;

import java.util.List;
import java.util.UUID;

public interface AdminPetManagementService {
    List<AdminPetSpeciesDTO> getPetSpecies();
    AdminPetSpeciesDTO createPetSpecies(AdminPetSpeciesDTO dto);
    AdminPetSpeciesDTO updatePetSpecies(UUID id, AdminPetSpeciesDTO dto);
    void deletePetSpecies(UUID id);

    List<AdminEggItemDTO> getEggItems();
    AdminEggItemDTO createEggItem(AdminEggItemDTO dto);
    AdminEggItemDTO updateEggItem(UUID id, AdminEggItemDTO dto);
    void deleteEggItem(UUID id);

    List<AdminEggDropRateDTO> getDropRates();
    AdminEggDropRateDTO createDropRate(AdminEggDropRateDTO dto);
    AdminEggDropRateDTO updateDropRate(UUID id, AdminEggDropRateDTO dto);
    void deleteDropRate(UUID id);
}

package com.example.chemlearn.gamification.service.impl;

import com.example.chemlearn.gamification.dto.admin.AdminEggDropRateDTO;
import com.example.chemlearn.gamification.dto.admin.AdminEggItemDTO;
import com.example.chemlearn.gamification.dto.admin.AdminPetSpeciesDTO;
import com.example.chemlearn.gamification.entity.EggDropRate;
import com.example.chemlearn.gamification.entity.Item;
import com.example.chemlearn.gamification.entity.PetSpecies;
import com.example.chemlearn.gamification.enums.ItemType;
import com.example.chemlearn.gamification.repository.EggDropRateRepository;
import com.example.chemlearn.gamification.repository.ItemRepository;
import com.example.chemlearn.gamification.repository.PetSpeciesRepository;
import com.example.chemlearn.gamification.service.AdminPetManagementService;
import com.example.chemlearn.lms.exception.CustomExceptions;
import com.example.chemlearn.lms.service.AutoMailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPetManagementServiceImpl implements AdminPetManagementService {
    private final PetSpeciesRepository petSpeciesRepository;
    private final ItemRepository itemRepository;
    private final EggDropRateRepository eggDropRateRepository;
    private final AutoMailNotificationService autoMailNotificationService;

    @Override
    public List<AdminPetSpeciesDTO> getPetSpecies() {
        return petSpeciesRepository.findAll().stream().map(this::toPetDto).toList();
    }

    @Override
    @Transactional
    public AdminPetSpeciesDTO createPetSpecies(AdminPetSpeciesDTO dto) {
        PetSpecies pet = new PetSpecies();
        applyPetDto(pet, dto);
        return toPetDto(petSpeciesRepository.save(pet));
    }

    @Override
    @Transactional
    public AdminPetSpeciesDTO updatePetSpecies(UUID id, AdminPetSpeciesDTO dto) {
        PetSpecies pet = petSpeciesRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Pet species not found"));
        applyPetDto(pet, dto);
        return toPetDto(petSpeciesRepository.save(pet));
    }

    @Override
    @Transactional
    public void deletePetSpecies(UUID id) {
        if (!petSpeciesRepository.existsById(id)) {
            throw new CustomExceptions.ResourceNotFoundException("Pet species not found");
        }
        petSpeciesRepository.deleteById(id);
    }

    @Override
    public List<AdminEggItemDTO> getEggItems() {
        return itemRepository.findByItemTypeOrderByNameAsc(ItemType.EGG).stream().map(this::toEggDto).toList();
    }

    @Override
    @Transactional
    public AdminEggItemDTO createEggItem(AdminEggItemDTO dto) {
        Item item = new Item();
        item.setItemType(ItemType.EGG);
        applyEggDto(item, dto);
        Item savedItem = itemRepository.save(item);
        autoMailNotificationService.notifyShopItemCreated(savedItem);
        return toEggDto(savedItem);
    }

    @Override
    @Transactional
    public AdminEggItemDTO updateEggItem(UUID id, AdminEggItemDTO dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Egg item not found"));
        Integer previousPrice = item.getPriceCoins();
        item.setItemType(ItemType.EGG);
        applyEggDto(item, dto);
        Item savedItem = itemRepository.save(item);
        autoMailNotificationService.notifyShopItemUpdated(savedItem, previousPrice);
        return toEggDto(savedItem);
    }

    @Override
    @Transactional
    public void deleteEggItem(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new CustomExceptions.ResourceNotFoundException("Egg item not found");
        }
        itemRepository.deleteById(id);
    }

    @Override
    public List<AdminEggDropRateDTO> getDropRates() {
        return eggDropRateRepository.findAll().stream().map(this::toDropDto).toList();
    }

    @Override
    @Transactional
    public AdminEggDropRateDTO createDropRate(AdminEggDropRateDTO dto) {
        EggDropRate rate = new EggDropRate();
        applyDropDto(rate, dto);
        return toDropDto(eggDropRateRepository.save(rate));
    }

    @Override
    @Transactional
    public AdminEggDropRateDTO updateDropRate(UUID id, AdminEggDropRateDTO dto) {
        EggDropRate rate = eggDropRateRepository.findById(id)
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Drop rate not found"));
        applyDropDto(rate, dto);
        return toDropDto(eggDropRateRepository.save(rate));
    }

    @Override
    @Transactional
    public void deleteDropRate(UUID id) {
        if (!eggDropRateRepository.existsById(id)) {
            throw new CustomExceptions.ResourceNotFoundException("Drop rate not found");
        }
        eggDropRateRepository.deleteById(id);
    }

    private void applyPetDto(PetSpecies pet, AdminPetSpeciesDTO dto) {
        pet.setName(dto.getName().trim());
        pet.setElement(dto.getElement().trim().toUpperCase());
        pet.setRarity(dto.getRarity().trim().toUpperCase());
        pet.setBaseHp(dto.getBaseHp());
        pet.setBaseDamage(dto.getBaseDamage());
        pet.setHpGrowth(dto.getHpGrowth());
        pet.setDamageGrowth(dto.getDamageGrowth());
        pet.setSkillName(dto.getSkillName());
        pet.setSkillDescription(dto.getSkillDescription());
        pet.setImageUrl(dto.getImageUrl());
    }

    private void applyEggDto(Item item, AdminEggItemDTO dto) {
        item.setName(dto.getName().trim());
        item.setDescription(dto.getDescription());
        item.setPriceCoins(dto.getPriceCoins());
        item.setEffectValue(dto.getEffectValue());
        item.setImageUrl(dto.getImageUrl());
    }

    private void applyDropDto(EggDropRate rate, AdminEggDropRateDTO dto) {
        Item egg = itemRepository.findById(dto.getEggItemId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Egg item not found"));
        if (egg.getItemType() != ItemType.EGG) {
            throw new CustomExceptions.BadRequestException("Item must be an egg");
        }
        PetSpecies pet = petSpeciesRepository.findById(dto.getPetSpeciesId())
                .orElseThrow(() -> new CustomExceptions.ResourceNotFoundException("Pet species not found"));
        rate.setEggItem(egg);
        rate.setPetSpecies(pet);
        rate.setDropWeight(dto.getDropWeight());
    }

    private AdminPetSpeciesDTO toPetDto(PetSpecies pet) {
        AdminPetSpeciesDTO dto = new AdminPetSpeciesDTO();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setElement(pet.getElement());
        dto.setRarity(pet.getRarity());
        dto.setBaseHp(pet.getBaseHp());
        dto.setBaseDamage(pet.getBaseDamage());
        dto.setHpGrowth(pet.getHpGrowth());
        dto.setDamageGrowth(pet.getDamageGrowth());
        dto.setSkillName(pet.getSkillName());
        dto.setSkillDescription(pet.getSkillDescription());
        dto.setImageUrl(pet.getImageUrl());
        return dto;
    }

    private AdminEggItemDTO toEggDto(Item item) {
        AdminEggItemDTO dto = new AdminEggItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPriceCoins(item.getPriceCoins());
        dto.setEffectValue(item.getEffectValue());
        dto.setImageUrl(item.getImageUrl());
        return dto;
    }

    private AdminEggDropRateDTO toDropDto(EggDropRate rate) {
        AdminEggDropRateDTO dto = new AdminEggDropRateDTO();
        dto.setId(rate.getId());
        dto.setEggItemId(rate.getEggItem().getId());
        dto.setEggItemName(rate.getEggItem().getName());
        dto.setPetSpeciesId(rate.getPetSpecies().getId());
        dto.setPetSpeciesName(rate.getPetSpecies().getName());
        dto.setDropWeight(rate.getDropWeight());
        return dto;
    }
}

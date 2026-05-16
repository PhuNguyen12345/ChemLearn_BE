package com.example.chemlearn.gamification.service.impl;

import com.example.chemlearn.core.entity.Student;
import com.example.chemlearn.lms.repository.StudentRepository;
import com.example.chemlearn.gamification.dto.pet.GachaResultDTO;
import com.example.chemlearn.gamification.dto.pet.InventoryItemDTO;
import com.example.chemlearn.gamification.dto.pet.PetSpeciesDTO;
import com.example.chemlearn.gamification.dto.pet.StudentPetDTO;
import com.example.chemlearn.gamification.entity.*;
import com.example.chemlearn.gamification.enums.ItemType;
import com.example.chemlearn.gamification.repository.*;
import com.example.chemlearn.gamification.service.PetSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetSystemServiceImpl implements PetSystemService {

    private final StudentRepository studentRepository;
    private final ItemRepository itemRepository;
    private final StudentItemRepository studentItemRepository;
    private final PetSpeciesRepository petSpeciesRepository;
    private final StudentPetRepository studentPetRepository;
    private final StudentPetFragmentRepository fragmentRepository;
    private final EggDropRateRepository eggDropRateRepository;

    private Student getStudent(String username) {
        return studentRepository.findByUsers_Username(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }

    private PetSpeciesDTO mapToDTO(PetSpecies species) {
        return PetSpeciesDTO.builder()
                .id(species.getId())
                .name(species.getName())
                .element(species.getElement())
                .rarity(species.getRarity())
                .skillName(species.getSkillName())
                .skillDescription(species.getSkillDescription())
                .imageUrl(species.getImageUrl())
                .build();
    }

    private StudentPetDTO mapToDTO(StudentPet pet) {
        int hp = pet.getSpecies().getBaseHp() + (pet.getLevel() - 1) * pet.getSpecies().getHpGrowth();
        int damage = pet.getSpecies().getBaseDamage() + (pet.getLevel() - 1) * pet.getSpecies().getDamageGrowth();
        
        // Star multiplier (e.g., 20% boost per star after 1)
        double multiplier = 1.0 + (pet.getStarLevel() - 1) * 0.2;
        
        return StudentPetDTO.builder()
                .id(pet.getId())
                .species(mapToDTO(pet.getSpecies()))
                .level(pet.getLevel())
                .experience(pet.getExperience())
                .starLevel(pet.getStarLevel())
                .maxHp((int) (hp * multiplier))
                .damage((int) (damage * multiplier))
                .nextLevelExp(pet.getLevel() * 100)
                .build();
    }

    @Override
    public List<StudentPetDTO> getMyPets(String username) {
        Student student = getStudent(username);
        return studentPetRepository.findByStudentId(student.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItemDTO> getMyInventory(String username) {
        Student student = getStudent(username);
        return studentItemRepository.findByStudentId(student.getId()).stream()
                .map(si -> InventoryItemDTO.builder()
                        .id(si.getId())
                        .itemId(si.getItem().getId())
                        .name(si.getItem().getName())
                        .itemType(si.getItem().getItemType().name())
                        .description(si.getItem().getDescription())
                        .quantity(si.getQuantity())
                        .imageUrl(si.getItem().getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItemDTO> getAllShopItems() {
        return itemRepository.findAll().stream()
                .map(item -> InventoryItemDTO.builder()
                        .id(item.getId()) 
                        .itemId(item.getId())
                        .name(item.getName())
                        .itemType(item.getItemType().name())
                        .description(item.getDescription())
                        .quantity(item.getPriceCoins()) // Price transported as quantity
                        .imageUrl(item.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public int getCoins(String username) {
        return getStudent(username).getCoins();
    }

    @Override
    @Transactional
    public void buyItem(String username, UUID itemId, int quantity) {
        Student student = getStudent(username);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));

        int totalCost = item.getPriceCoins() * quantity;
        if (student.getCoins() < totalCost) {
            throw new IllegalArgumentException("Not enough coins");
        }

        student.setCoins(student.getCoins() - totalCost);
        studentRepository.save(student);

        StudentItem studentItem = studentItemRepository.findByStudentIdAndItemId(student.getId(), itemId)
                .orElseGet(() -> {
                    StudentItem newItem = new StudentItem();
                    newItem.setStudent(student);
                    newItem.setItem(item);
                    return newItem;
                });
        studentItem.setQuantity(studentItem.getQuantity() + quantity);
        studentItemRepository.save(studentItem);
    }

    @Override
    @Transactional
    public GachaResultDTO openEgg(String username, UUID eggItemId) {
        Student student = getStudent(username);
        
        // 1. Consume Egg
        StudentItem eggItem = studentItemRepository.findByStudentIdAndItemId(student.getId(), eggItemId)
                .orElseThrow(() -> new IllegalArgumentException("You do not have this egg"));
        
        if (eggItem.getQuantity() < 1) {
            throw new IllegalArgumentException("You do not have this egg");
        }
        if (eggItem.getItem().getItemType() != ItemType.EGG) {
            throw new IllegalArgumentException("Item is not an egg");
        }
        
        eggItem.setQuantity(eggItem.getQuantity() - 1);
        studentItemRepository.save(eggItem);

        // 2. Gacha Logic
        List<EggDropRate> dropRates = eggDropRateRepository.findByEggItemId(eggItemId);
        if (dropRates.isEmpty()) throw new IllegalStateException("This egg has no drops configured");

        int totalWeight = dropRates.stream().mapToInt(EggDropRate::getDropWeight).sum();
        int randomValue = new Random().nextInt(totalWeight);
        
        PetSpecies pulledSpecies = null;
        int currentWeight = 0;
        for (EggDropRate rate : dropRates) {
            currentWeight += rate.getDropWeight();
            if (randomValue < currentWeight) {
                pulledSpecies = rate.getPetSpecies();
                break;
            }
        }

        // 3. Process Result
        boolean isDuplicate = studentPetRepository.findByStudentIdAndSpeciesId(student.getId(), pulledSpecies.getId()).isPresent();
        int fragmentsReceived = 0;

        final PetSpecies finalPulledSpecies = pulledSpecies;
        if (isDuplicate) {
            StudentPetFragment fragment = fragmentRepository.findByStudentIdAndSpeciesId(student.getId(), finalPulledSpecies.getId())
                    .orElseGet(() -> {
                        StudentPetFragment f = new StudentPetFragment();
                        f.setStudent(student);
                        f.setSpecies(finalPulledSpecies);
                        return f;
                    });
            fragmentsReceived = 10; // e.g. duplicate gives 10 fragments
            fragment.setAmount(fragment.getAmount() + fragmentsReceived);
            fragmentRepository.save(fragment);
        } else {
            StudentPet newPet = new StudentPet();
            newPet.setStudent(student);
            newPet.setSpecies(pulledSpecies);
            studentPetRepository.save(newPet);
        }

        return GachaResultDTO.builder()
                .species(mapToDTO(pulledSpecies))
                .isDuplicate(isDuplicate)
                .fragmentsReceived(fragmentsReceived)
                .build();
    }

    @Override
    @Transactional
    public void feedPet(String username, UUID studentPetId, UUID foodItemId, int quantity) {
        Student student = getStudent(username);
        StudentPet pet = studentPetRepository.findById(studentPetId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        
        if (!pet.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Not your pet");
        }

        StudentItem foodItem = studentItemRepository.findByStudentIdAndItemId(student.getId(), foodItemId)
                .orElseThrow(() -> new IllegalArgumentException("Food not found"));
        
        if (foodItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough food");
        }
        if (foodItem.getItem().getItemType() != ItemType.FOOD) {
            throw new IllegalArgumentException("Item is not food");
        }

        foodItem.setQuantity(foodItem.getQuantity() - quantity);
        studentItemRepository.save(foodItem);

        int expGained = foodItem.getItem().getEffectValue() * quantity;
        pet.setExperience(pet.getExperience() + expGained);

        // Level up logic
        int expNeeded = pet.getLevel() * 100;
        while (pet.getExperience() >= expNeeded) {
            pet.setExperience(pet.getExperience() - expNeeded);
            pet.setLevel(pet.getLevel() + 1);
            expNeeded = pet.getLevel() * 100;
        }

        studentPetRepository.save(pet);
    }

    @Override
    @Transactional
    public void starUpPet(String username, UUID studentPetId) {
        Student student = getStudent(username);
        StudentPet pet = studentPetRepository.findById(studentPetId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        
        if (!pet.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Not your pet");
        }
        
        if (pet.getStarLevel() >= 5) {
            throw new IllegalArgumentException("Pet is already at max star level");
        }

        int fragmentsNeeded = pet.getStarLevel() * 20; // 20, 40, 60, 80
        StudentPetFragment fragment = fragmentRepository.findByStudentIdAndSpeciesId(student.getId(), pet.getSpecies().getId())
                .orElseThrow(() -> new IllegalArgumentException("No fragments found"));
                
        if (fragment.getAmount() < fragmentsNeeded) {
            throw new IllegalArgumentException("Not enough fragments to star up");
        }

        fragment.setAmount(fragment.getAmount() - fragmentsNeeded);
        fragmentRepository.save(fragment);

        pet.setStarLevel(pet.getStarLevel() + 1);
        studentPetRepository.save(pet);
    }
}

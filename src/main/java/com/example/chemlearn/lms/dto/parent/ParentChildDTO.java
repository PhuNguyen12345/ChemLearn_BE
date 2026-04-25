package com.example.chemlearn.lms.dto.parent;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ParentChildDTO {
    private UUID id;
    private String username;
    private String email;
}


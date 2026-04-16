package com.example.chemlearn.dtos.parent;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParentChildDTO {
    private Long id;
    private String username;
    private String email;
}

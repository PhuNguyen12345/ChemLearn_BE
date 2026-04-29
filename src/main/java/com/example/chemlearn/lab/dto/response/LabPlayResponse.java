package com.example.chemlearn.lab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LabPlayResponse {
    private UUID labId;
    private String title;
    private String type;
    private Object workspace;
    private Object config;
}

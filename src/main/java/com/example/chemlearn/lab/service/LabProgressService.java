package com.example.chemlearn.lab.service;


import com.example.chemlearn.lab.dto.request.SaveProgressRequest;
import com.example.chemlearn.lab.dto.response.LabPlayResponse;

import java.util.UUID;

public interface LabProgressService {
    LabPlayResponse playLab(UUID labId, UUID studentId);
    void saveProgress(UUID studentId, UUID labId, SaveProgressRequest request);
    void submitLab(UUID studentId, UUID labId);
    void resetLab(UUID studentId, UUID labId);
}

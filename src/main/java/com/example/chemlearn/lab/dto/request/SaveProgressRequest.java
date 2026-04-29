package com.example.chemlearn.lab.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveProgressRequest {
    //JSON for current workspace
    private List<Map<String, Object>> currentWorkspace;
    // Viewport
    private Map<String, Object> viewport;
    //List of completed actions
    private List<Map<String, Object>> completedActions;
}

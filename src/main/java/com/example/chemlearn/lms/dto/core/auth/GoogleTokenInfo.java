package com.example.chemlearn.lms.dto.core.auth;

import lombok.Data;

@Data
public class GoogleTokenInfo {
    private String email;
    private String sub;
    private String name;
    private String picture;
}

package com.example.community_service.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommunityGroupRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "Group name is required")
    private String name;

    private String city;
    private String originCountry;

    @Size(max = 1000, message = "Description should not exceed 1000 words")
    private String description;

    // this field for public/private groups -> Default to public
    private Boolean isPublic;

    private String adminName;

}

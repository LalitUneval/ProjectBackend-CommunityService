package com.example.community_service.dto.community;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateGroupRequestDTO {

    @Size(max = 1000, message = "Description should not exceed 1000 words")
    private String description;

    // Add this field for changing in future to vice versa
    private Boolean isPublic;

}

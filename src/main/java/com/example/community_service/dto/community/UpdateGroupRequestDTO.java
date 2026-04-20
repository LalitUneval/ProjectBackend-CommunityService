package com.example.community_service.dto.community;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateGroupRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = 1000, message = "Description should not exceed 1000 words")
    private String description;

    // Add this field for changing in future to vice versa
    private Boolean isPublic;

}

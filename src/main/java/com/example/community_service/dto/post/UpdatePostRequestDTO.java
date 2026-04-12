package com.example.community_service.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequestDTO {

    @NotBlank(message = "Content is required")
    private String content;
}


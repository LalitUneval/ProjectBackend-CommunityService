package com.example.community_service.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequestDTO {

    // @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotBlank(message = "Content is required")
    private String content;
}


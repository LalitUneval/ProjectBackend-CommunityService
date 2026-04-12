package com.example.community_service.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {

    private Long id;
    private Long userId;
    private Long groupId;
    private String groupName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer likesCount;
    private Integer commentsCount;

}

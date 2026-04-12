package com.example.community_service.dto.community;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupResponseDTO {
    private Long groupId;
    private Long userId;
    private String status; // PENDING -> APPROVED / REJECTED
    private String message;
}

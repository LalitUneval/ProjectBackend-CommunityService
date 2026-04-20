package com.example.community_service.dto.community;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long groupId;
    private Long userId;
    private String status; // PENDING -> APPROVED / REJECTED
    private String message;
}

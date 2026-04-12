package com.example.community_service.dto.community;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDTO {
    private Long id;
    private Long groupId;
    private Long userId;
    private String userName;
    private String status; // PENDING, APPROVED, REJECTED
    private String role; // ADMIN, MEMBER
    private LocalDateTime joinedAt;
    private LocalDateTime requestedAt;
}

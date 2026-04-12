package com.example.community_service.entity.community;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED


    private String role ; // ADMIN, MEMBER

    private LocalDateTime joinedAt;
    private LocalDateTime requestedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
    }

}

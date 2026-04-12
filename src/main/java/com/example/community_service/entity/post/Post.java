package com.example.community_service.entity.post;

import com.example.community_service.entity.community.CommunityGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to UserProfile.id (cross-service reference)
    @Column(nullable = false)
    private Long userId;

    // Many-to-One relationship with CommunityGroup
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private CommunityGroup group;

    @Column(length = 5000, nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private Integer likesCount = 0;

    private Integer commentsCount = 0;
}

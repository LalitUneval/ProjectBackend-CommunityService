package com.example.community_service.entity.community;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    private String city;

    @Column(name = "origin_country")
    private String originCountry;

    @Column(length = 1000)
    private String description;

    // Adding these fields for public or private group
    @Column(nullable = false)
    private Boolean isPublic = true;

    // group owner (admin) id
    @Column(nullable = false)
    private Long createdBy;

    private LocalDateTime createdAt;

    private String adminName;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}

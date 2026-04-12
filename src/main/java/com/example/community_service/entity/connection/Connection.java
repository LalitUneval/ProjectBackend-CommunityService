package com.example.community_service.entity.connection;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to UserProfile.id (person who sent connection request)
    @Column(nullable = false)
    private Long senderId;

    // Foreign key to UserProfile.id (person who received connection request)
    @Column(nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionStatus status = ConnectionStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime acceptedAt;

}

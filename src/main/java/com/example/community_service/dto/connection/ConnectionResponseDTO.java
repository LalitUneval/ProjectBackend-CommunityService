package com.example.community_service.dto.connection;

import com.example.community_service.entity.connection.ConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionResponseDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private ConnectionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
}

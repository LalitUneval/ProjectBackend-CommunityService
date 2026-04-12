package com.example.community_service.dto.connection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendConnectionRequestDTO {
    @NotNull(message = "Sender Id is Required")
    private Long senderId;

    @NotNull(message = "Receiver Id is Required")
    private Long receiverId;
}

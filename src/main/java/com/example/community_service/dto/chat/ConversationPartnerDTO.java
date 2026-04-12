package com.example.community_service.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationPartnerDTO {

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("lastMessage")
    private String lastMessage;

    @JsonProperty("lastMessageTime")
    private String lastMessageTime;

    @JsonProperty("unreadCount")
    private long unreadCount;
}

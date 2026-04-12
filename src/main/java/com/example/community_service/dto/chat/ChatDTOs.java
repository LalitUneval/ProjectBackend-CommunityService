package com.example.community_service.dto.chat;

import com.example.community_service.entity.chat.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO classes for WebSocket chat messages.
 *
 * We never expose entity objects directly to avoid serialization issues
 * with lazy-loaded JPA fields.
 */
public class ChatDTOs {


    // For request


    /**
     * Payload the frontend sends when posting a GROUP message.
     * Frontend sends to: /app/chat.group
     *
     * Note: senderId is NOT in the request body — we read it from
     * the WebSocket session's Principal (set during CONNECT).
     * This prevents users from spoofing another user's ID.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupMessageRequest {
        private Long groupId;
        private String content;
        private String senderName; // Optional: frontend can pass display name
    }

    /**
     * Payload the frontend sends when posting a PRIVATE message.
     *
     * Frontend sends to: /app/chat.private
     * {
     *   "receiverId": 7,
     *   "content": "Hey, how are you?"
     * }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrivateMessageRequest {
        private Long receiverId;
        private String content;
        private String senderName;
    }



    // For Response



    /**
     * The message object delivered to all subscribers.
     *
     * Group subscribers receive this at: /topic/group.{groupId}
     * Private recipients receive this at: /user/queue/private
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessageResponse {
        private Long id;
        private Long senderId;
        private String senderName;
        private Long groupId;      // null for private messages
        private Long receiverId;   // null for group messages
        private String content;
        private ChatMessage.MessageType messageType;
        private ChatMessage.ChatType chatType;
        private LocalDateTime sentAt;
        private boolean read;
    }
}
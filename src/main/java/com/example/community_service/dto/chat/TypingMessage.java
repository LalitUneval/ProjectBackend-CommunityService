package com.example.community_service.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent when a user is typing.
 *
 * Frontend sends to:  /app/chat.typing
 * Backend forwards to: /user/{receiverId}/queue/typing  (private)
 *                   or /topic/group.{groupId}           (group)
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingMessage {

    // Who is typing
    private Long   senderId;
    private String senderName;

    // For PRIVATE typing: who to notify
    private Long   receiverId;

    // For GROUP typing: which group
    private Long   groupId;

    // "PRIVATE" or "GROUP"
    private String chatType;

    // true = started typing, false = stopped typing
    private boolean typing;
}
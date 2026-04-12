package com.example.community_service.controller.chat;

import com.example.community_service.dto.chat.ChatDTOs;
import com.example.community_service.dto.chat.TypingMessage;
import com.example.community_service.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * ChatWebSocketController — updated with typing indicator support.
 *
 * New endpoint:
 *   Frontend sends to /app/chat.typing
 *   Backend routes to receiver's private queue OR group topic
 *   No DB write — purely in-memory real-time signal
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // Existing message handlers

    @MessageMapping("/chat.group")
    public void handleGroupMessage(
            @Payload ChatDTOs.GroupMessageRequest request,
            Principal principal) {
        if (principal == null) return;
        Long senderId = Long.parseLong(principal.getName());
        chatService.sendGroupMessage(senderId, request);
    }

    @MessageMapping("/chat.private")
    public void handlePrivateMessage(
            @Payload ChatDTOs.PrivateMessageRequest request,
            Principal principal) {
        if (principal == null) return;
        Long senderId = Long.parseLong(principal.getName());
        chatService.sendPrivateMessage(senderId, request);
    }


    /**
     * Handles typing notifications.
     *
     * Frontend sends to: /app/chat.typing
     * Payload example:
     * {
     *   "receiverId": 7,
     *   "chatType": "PRIVATE",
     *   "typing": true,
     *   "senderName": "Milan"
     * }
     *
     * For PRIVATE: delivers to /user/{receiverId}/queue/typing
     * For GROUP:   delivers to /topic/group.{groupId}  (with event type "TYPING")
     *
     * No database write — this is fire-and-forget real-time only.
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(
            @Payload TypingMessage payload,
            Principal principal) {

        if (principal == null) return;

        // Set the actual senderId from the authenticated session
        Long senderId = Long.parseLong(principal.getName());
        payload.setSenderId(senderId);

        if ("PRIVATE".equals(payload.getChatType()) && payload.getReceiverId() != null) {

            // Deliver typing signal only to the receiver
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(payload.getReceiverId()),
                    "/queue/typing",
                    payload
            );

            log.debug("Typing signal: user {} → user {} [typing={}]",
                    senderId, payload.getReceiverId(), payload.isTyping());

        } else if ("GROUP".equals(payload.getChatType()) && payload.getGroupId() != null) {

            // Broadcast typing signal to the whole group
            messagingTemplate.convertAndSend(
                    "/topic/group." + payload.getGroupId() + ".typing",
                    payload
            );

            log.debug("Typing signal: user {} → group {} [typing={}]",
                    senderId, payload.getGroupId(), payload.isTyping());
        }
    }
}
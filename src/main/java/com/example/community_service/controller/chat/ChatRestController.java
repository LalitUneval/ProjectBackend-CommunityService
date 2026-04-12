package com.example.community_service.controller.chat;

import com.example.community_service.dto.chat.ChatDTOs;
import com.example.community_service.dto.chat.ConversationPartnerDTO;
import com.example.community_service.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ChatRestController
 *
 * These are regular HTTP (REST) endpoints — NOT WebSocket.
 *
 * Why REST for chat history?
 * WebSocket is for REAL-TIME delivery of NEW messages.
 * But when a user opens a chat for the first time, they need to load
 * OLD messages. That's a one-time "fetch" — perfect for REST.
 *
 * Pattern:
 * - Load history   → HTTP GET (REST)
 * - Send/receive   → WebSocket (real-time)
 */
@RestController
@RequestMapping("/api/community/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatRestController {

    private final ChatService chatService;

    /**
     * Get chat history for a group.
     * Called when user opens a group chat room.
     *
     * GET /api/community/chat/group/{groupId}/history?limit=50
     */
    @GetMapping("/group/{groupId}/history")
    public ResponseEntity<List<ChatDTOs.ChatMessageResponse>> getGroupHistory(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "50") int limit) {

        return ResponseEntity.ok(chatService.getGroupHistory(groupId, limit));
    }

    /**
     * Get private conversation history between two users.
     * Called when user opens a DM chat window.
     *
     * GET /api/community/chat/private/{otherUserId}
     */
    @GetMapping("/private/{otherUserId}")
    public ResponseEntity<List<ChatDTOs.ChatMessageResponse>> getPrivateHistory(
            @PathVariable Long otherUserId,
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(chatService.getPrivateConversation(userId, otherUserId));
    }

    /**
     * Mark messages from a specific sender as read.
     * Called when user opens a DM chat window marks messages as seen
     *
     * PUT /api/community/chat/private/{senderId}/read
     */
    @PutMapping("/private/{senderId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId,
            @RequestHeader("X-User-Id") Long userId) {

        chatService.markAsRead(userId, senderId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get unread message count for the current user
     * Useful for showing notification
     *
     * GET /api/community/chat/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(chatService.countUnreadMessages(userId));
    }

    /**
     * Get all unique conversation partners for the current user.
     * Returns a list of userIds that the current user has exchanged messages with.
     *
     * GET /api/community/chat/conversations
     *
     * Frontend calls this on load to populate the sidebar with past conversations.
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<Long>> getConversationPartners(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(chatService.getConversationPartners(userId));
    }




}
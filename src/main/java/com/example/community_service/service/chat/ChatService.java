package com.example.community_service.service.chat;

import com.example.community_service.dto.chat.ChatDTOs;
import com.example.community_service.entity.chat.ChatMessage;
import com.example.community_service.exception.GroupNotFoundException;
import com.example.community_service.repository.chat.ChatMessageRepository;
import com.example.community_service.repository.community.CommunityRepository;
import com.example.community_service.repository.community.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatService
 *
 * 1. Validate that the user is allowed to send in this group/conversation
 * 2. Save the message to the database
 * 3. Broadcast/deliver the message via WebSocket
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CommunityRepository communityRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Group chat

    /**
     * Send a message to a group.
     *
     * Broadcast to /topic/group.{groupId}
     */
    public ChatDTOs.ChatMessageResponse sendGroupMessage(
            Long senderId,
            ChatDTOs.GroupMessageRequest request) {

        // Validate if group exists or not
        var group = communityRepository.findById(request.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + request.getGroupId()));

        // Validate sender is an approved member
        var membership = groupMemberRepository.findByGroupIdAndUserId(request.getGroupId(), senderId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));

        if (!"APPROVED".equals(membership.getStatus())) {
            throw new RuntimeException("Your membership is not approved yet");
        }

        // Build and save the message
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .senderName(request.getSenderName() != null ? request.getSenderName() : "User " + senderId)
                .groupId(request.getGroupId())
                .content(request.getContent())
                .messageType(ChatMessage.MessageType.CHAT)
                .chatType(ChatMessage.ChatType.GROUP)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Convert to response DTO
        ChatDTOs.ChatMessageResponse response = toResponse(saved);

        // Broadcast to group topic
        // All users subscribed to /topic/group.{groupId} receive this
        messagingTemplate.convertAndSend(
                "/topic/group." + request.getGroupId(),
                response
        );

        log.info("Group message sent by user {} to group {}", senderId, request.getGroupId());
        return response;
    }

    /**
     * Broadcast a JOIN notification to the group.
     */
    public void broadcastJoinMessage(Long groupId, Long userId, String userName) {
        ChatMessage message = ChatMessage.builder()
                .senderId(userId)
                .senderName(userName)
                .groupId(groupId)
                .content(userName + " has joined the group")
                .messageType(ChatMessage.MessageType.JOIN)
                .chatType(ChatMessage.ChatType.GROUP)
                .build();

        chatMessageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/group." + groupId,
                toResponse(message)
        );
    }

    /**
     * Broadcast a LEAVE notification to the group.
     */
    public void broadcastLeaveMessage(Long groupId, Long userId, String userName) {
        ChatMessage message = ChatMessage.builder()
                .senderId(userId)
                .senderName(userName)
                .groupId(groupId)
                .content(userName + " has left the group")
                .messageType(ChatMessage.MessageType.LEAVE)
                .chatType(ChatMessage.ChatType.GROUP)
                .build();

        chatMessageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/group." + groupId,
                toResponse(message)
        );
    }



    // Private chat (One-to-One)

    /**
     * Send a private message from one user to another.
     *
     * convertAndSendToUser(receiverId, "/queue/private", message)
     * → Spring translates this to: /user/{receiverId}/queue/private
     * → Only the user with Principal.name == receiverId gets it
     *
     * The sender also gets a copy (sent to their own /queue/private)
     * so they see the message appear in their own chat window.
     */
    public ChatDTOs.ChatMessageResponse sendPrivateMessage(
            Long senderId,
            ChatDTOs.PrivateMessageRequest request) {

        // Build and save message
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .senderName(request.getSenderName() != null ? request.getSenderName() : "User " + senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .messageType(ChatMessage.MessageType.CHAT)
                .chatType(ChatMessage.ChatType.PRIVATE)
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatDTOs.ChatMessageResponse response = toResponse(saved);

        // Deliver to RECEIVER
        // convertAndSendToUser uses the Principal name to route correctly
        messagingTemplate.convertAndSendToUser(
                String.valueOf(request.getReceiverId()),
                "/queue/private",
                response
        );

        // Also deliver to SENDER for their UI updates
        messagingTemplate.convertAndSendToUser(
                String.valueOf(senderId),
                "/queue/private",
                response
        );

        log.info("Private message sent from user {} to user {}", senderId, request.getReceiverId());
        return response;
    }



    // HISTORY

    /**
     * Get last N messages in a group (for loading chat history when user opens the group).
     */
    public List<ChatDTOs.ChatMessageResponse> getGroupHistory(Long groupId, int limit) {
        var messages = chatMessageRepository
                .findByGroupIdOrderBySentAtDesc(groupId, PageRequest.of(0, limit));

        // Return in chronological order (oldest first)
        return messages.stream()
                .sorted((a, b) -> a.getSentAt().compareTo(b.getSentAt()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get full private conversation between two users.
     */
    public List<ChatDTOs.ChatMessageResponse> getPrivateConversation(Long userId1, Long userId2) {
        return chatMessageRepository
                .findPrivateConversation(userId1, userId2)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mark all messages from a sender as read by the receiver.
     * Called when receiver opens the chat window.
     */
    public void markAsRead(Long receiverId, Long senderId) {
        chatMessageRepository.markMessagesAsRead(senderId, receiverId);
    }

    /**
     * Count total unread messages for a user.
     */
    public long countUnreadMessages(Long userId) {
        return chatMessageRepository.countUnreadMessages(userId);
    }

    public List<Long> getConversationPartners(Long userId) {
        return chatMessageRepository.findConversationPartners(userId);
    }



    // Helper method

    private ChatDTOs.ChatMessageResponse toResponse(ChatMessage msg) {
        return ChatDTOs.ChatMessageResponse.builder()
                .id(msg.getId())
                .senderId(msg.getSenderId())
                .senderName(msg.getSenderName())
                .groupId(msg.getGroupId())
                .receiverId(msg.getReceiverId())
                .content(msg.getContent())
                .messageType(msg.getMessageType())
                .chatType(msg.getChatType())
                .sentAt(msg.getSentAt())
                .read(msg.isRead())
                .build();
    }
}
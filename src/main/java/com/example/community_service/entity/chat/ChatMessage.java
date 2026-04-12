package com.example.community_service.entity.chat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ChatMessage Entity
 *
 * Stores all chat messages in the database for history/persistence.
 *
 * MESSAGE_TYPE:
 * - CHAT    → a regular text message
 * - JOIN    → system message: "User X joined the group"
 * - LEAVE   → system message: "User X left the group"
 *
 * CHAT_TYPE:
 * - GROUP   → sent to a group room (/topic/group.{groupId})
 * - PRIVATE → sent to a specific user (/queue/user.{receiverId})
 */
@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId;

    // display name of sender (stored for quick retrieval, no join needed)
    private String senderName;

    // for GROUP messages
    private Long groupId;

    // for PRIVATE messages
    private Long receiverId;

    // text content
    @Column(length = 5000, nullable = false)
    private String content;

    // CHAT, JOIN, LEAVE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    // GROUP or PRIVATE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType chatType;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    // receiver read this message or not? (for private messages)
    private boolean isRead = false;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    public enum MessageType {
        CHAT,   // Normal message
        JOIN,   // "User joined" system message
        LEAVE   // "User left" system message
    }

    public enum ChatType {
        GROUP,   // Group chat message
        PRIVATE  // One-to-one private message
    }
}
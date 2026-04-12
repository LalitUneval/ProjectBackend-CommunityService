package com.example.community_service.repository.chat;

import com.example.community_service.entity.chat.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get recent messages for a group (paginated), newest first
    List<ChatMessage> findByGroupIdOrderBySentAtDesc(Long groupId, Pageable pageable);

    // Get full group chat history
    List<ChatMessage> findByGroupIdOrderBySentAtAsc(Long groupId);

    // Get private conversation between two users (both directions)
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.chatType = 'PRIVATE'
          AND ((m.senderId = :userId1 AND m.receiverId = :userId2)
            OR (m.senderId = :userId2 AND m.receiverId = :userId1))
        ORDER BY m.sentAt ASC
        """)
    List<ChatMessage> findPrivateConversation(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    // Count unread messages for a user
    @Query("""
        SELECT COUNT(m) FROM ChatMessage m
        WHERE m.receiverId = :userId
          AND m.isRead = false
          AND m.chatType = 'PRIVATE'
        """)
    long countUnreadMessages(@Param("userId") Long userId);

    // Count unread messages from a specific sender
    @Query("""
        SELECT COUNT(m) FROM ChatMessage m
        WHERE m.receiverId = :receiverId
          AND m.senderId = :senderId
          AND m.isRead = false
          AND m.chatType = 'PRIVATE'
        """)
    long countUnreadFrom(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

    // Mark all messages from sender to receiver as read
    @Modifying
    @Query("""
        UPDATE ChatMessage m
        SET m.isRead = true
        WHERE m.senderId = :senderId
          AND m.receiverId = :receiverId
          AND m.isRead = false
        """)
    void markMessagesAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query("""
    SELECT DISTINCT
        CASE
            WHEN m.senderId = :userId THEN m.receiverId
            ELSE m.senderId
        END
    FROM ChatMessage m
    WHERE m.chatType = 'PRIVATE'
      AND (m.senderId = :userId OR m.receiverId = :userId)
    """)
    List<Long> findConversationPartners(@Param("userId") Long userId);



    // Added this new query (keep the old findConversationPartners)
    @Query("""
    SELECT m FROM ChatMessage m
    WHERE m.chatType = 'PRIVATE'
      AND ((m.senderId = :userId AND m.receiverId = :partnerId)
        OR (m.senderId = :partnerId AND m.receiverId = :userId))
    ORDER BY m.sentAt DESC
    """)
    List<ChatMessage> findMessagesBetween(
            @Param("userId") Long userId,
            @Param("partnerId") Long partnerId,
            Pageable pageable);

}
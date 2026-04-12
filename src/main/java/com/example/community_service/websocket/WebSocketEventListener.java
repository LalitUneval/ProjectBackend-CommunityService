package com.example.community_service.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocketEventListener
 *
 * Listens for WebSocket connection and disconnection events.
 * This is useful for:
 * - Tracking online users
 * - Logging connect/disconnect for debugging
 * - Cleaning up state when a user disconnects
 *
 * onlineUsers map: userId → sessionId
 * ConcurrentHashMap is thread-safe — multiple users connect simultaneously.
 */
@Component
@Slf4j
public class WebSocketEventListener {

    // Tracks currently connected users: userId → sessionId
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();

    /**
     * Fired when a client successfully connects via WebSocket.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String userId = (headerAccessor.getUser() != null)
                ? headerAccessor.getUser().getName()
                : "unknown";

        onlineUsers.put(userId, sessionId);
        log.info("✅ WebSocket CONNECTED — userId: {}, sessionId: {}", userId, sessionId);
    }

    /**
     * Fired when a client disconnects (closes browser, loses network, etc.)
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String userId = (headerAccessor.getUser() != null)
                ? headerAccessor.getUser().getName()
                : "unknown";

        onlineUsers.remove(userId);
        log.info("❌ WebSocket DISCONNECTED — userId: {}, sessionId: {}", userId, sessionId);
    }

    /**
     * Check if a user is currently online.
     * Can be used by services to decide whether to send push notifications.
     */
    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(String.valueOf(userId));
    }

    /**
     * Get count of currently connected users.
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
}
package com.example.community_service.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * AuthChannelInterceptor
 *
 * WebSocket doesn't carry HTTP headers like Authorization — once the WebSocket
 * connection is established, it's a persistent socket, not an HTTP request.
 *
 * Solution: The frontend sends the userId as a STOMP header when connecting.
 * This interceptor reads that header and sets it as the "Principal" (identity)
 * of the WebSocket session.
 *
 * Principal = the logged-in user identity for a WebSocket session.
 * Spring uses this for /user/queue/... delivery (sends to the right person).
 *
 * Flow:
 *   Frontend → STOMP CONNECT frame with header: userId=42
 *   This interceptor → reads userId=42 → sets Principal(name="42")
 *   Now Spring knows: this WebSocket session belongs to user 42
 */
@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Print all headers send by frontend
            System.out.println("=== STOMP CONNECT headers: " + accessor.toNativeHeaderMap());

            // Try every possible casing
            String userId = accessor.getFirstNativeHeader("userId");
            if (userId == null) userId = accessor.getFirstNativeHeader("userid");
            if (userId == null) userId = accessor.getFirstNativeHeader("user-id");
            if (userId == null) userId = accessor.getFirstNativeHeader("UserId");
            if (userId == null) userId = accessor.getFirstNativeHeader("X-User-Id");

            System.out.println("=== Resolved userId: " + userId);

            if (userId != null && !userId.isBlank()) {
                final String id = userId;
                accessor.setUser(() -> id);
            }
        }

        return message;
    }

    /**
     * Simple Principal implementation that holds the userId as the name.
     * Spring's SimpMessagingTemplate uses this name for user-specific delivery.
     */
    public static class StompPrincipal implements Principal {
        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
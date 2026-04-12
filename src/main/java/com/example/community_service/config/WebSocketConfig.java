package com.example.community_service.config;



import com.example.community_service.websocket.AuthChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration
 *
 * STOMP (Simple Text Oriented Messaging Protocol) is a messaging protocol
 * that runs on top of WebSocket. Think of it like this:
 *
 * - WebSocket = the "pipe" (raw connection)
 * - STOMP     = the "language" spoken inside the pipe
 *
 * Key Concepts:
 * - Message Broker: A router that delivers messages to the right destination
 * - /topic/**  : Broadcast channel (one message → many subscribers) → used for GROUP CHAT
 * - /queue/**  : Personal channel (one message → one user)           → used for PRIVATE CHAT
 * - /app/**    : Prefix for endpoints handled by @MessageMapping methods
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;

    public WebSocketConfig(AuthChannelInterceptor authChannelInterceptor) {
        this.authChannelInterceptor = authChannelInterceptor;
    }

    /**
     * Registers the WebSocket handshake endpoint.
     *
     * Frontend connects to: ws://localhost:8090/ws
     *
     * SockJS is a fallback library — if the browser doesn't support WebSocket,
     * it falls back to HTTP long-polling. Always include it for compatibility.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // In production: replace with your frontend URL
                .withSockJS();                   // Enable SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for destinations handled by @MessageMapping in controllers
        registry.setApplicationDestinationPrefixes("/app");

        // Enable simple in-memory broker for /topic and /queue
        // /topic → group/broadcast messages
        // /queue → private/personal messages
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for user-specific destinations
        // /user/queue/... → delivers only to specific connected user
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Register the channel interceptor that reads userId from STOMP headers.
     * This runs BEFORE any message is processed — it's our authentication layer.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
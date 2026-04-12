package com.example.community_service.controller.connection;

import com.example.community_service.dto.connection.ConnectionResponseDTO;
import com.example.community_service.dto.connection.SendConnectionRequestDTO;
import com.example.community_service.service.connection.ConnectionService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community/connections")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConnectionController {
    private final ConnectionService connectionService;

    /**
     * Send connection request
     * POST /api/community/connections
     */
    @PostMapping
    public ResponseEntity<ConnectionResponseDTO> sendConnectionRequest(
            @Valid @RequestBody SendConnectionRequestDTO request,
            @RequestHeader("X-User-Id") Long userId ) {

        // Set senderId from header
        request.setSenderId(userId);
        ConnectionResponseDTO response = connectionService.sendConnectionRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Accept connection request
     * PUT /api/community/connections/{connectionId}/accept
     */
    @PutMapping("/{connectionId}/accept")
    public ResponseEntity<ConnectionResponseDTO> acceptConnection(
                @PathVariable Long connectionId,
                @RequestHeader("X-User-Id") Long userId ) {
        ConnectionResponseDTO response = connectionService.acceptConnection(connectionId, userId);
        return ResponseEntity.ok(response);
    }


    /**
     * Reject connection request
     * PUT /api/community/connections/{connectionId}/reject
     */
    @PutMapping("/{connectionId}/reject")
    public ResponseEntity<Void> rejectConnection(
            @PathVariable Long connectionId,
            @RequestHeader("X-User-Id") Long userId ) {
        connectionService.rejectConnection(connectionId,userId);
        return ResponseEntity.ok().build();
    }


    /**
     * Remove connection
     * DELETE /api/community/connections/{connectionId}
     */
    @DeleteMapping("/{connectionId}")
    public ResponseEntity<Void> deleteConnection(
            @PathVariable Long connectionId,
            @RequestHeader("X-User-Id") Long userId ) {
        connectionService.removeConnection(connectionId, userId);
        return ResponseEntity.noContent().build();
    }


    /**
     * Get user's connections
     * GET /api/community/connections/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConnectionResponseDTO>> getUserConnections(@PathVariable Long userId) {
        List<ConnectionResponseDTO> connections = connectionService.getUserConnections(userId);
        return ResponseEntity.ok(connections);
    }


    /**
     * Get accepted connections
     * GET /api/community/connections/accepted
     */
    @GetMapping("/accepted")
    public ResponseEntity<List<ConnectionResponseDTO>> getAcceptedConnections (
            @RequestHeader("x-User-Id") Long userId ) {
        List<ConnectionResponseDTO> connections = connectionService.getAcceptedConnections(userId);
        return ResponseEntity.ok(connections);
    }


    /**
     * Get pending requests (received)
     * GET /api/community/connections/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ConnectionResponseDTO>> getPendingRequests (@RequestHeader("X-User-Id") Long userId) {
        List<ConnectionResponseDTO> connections = connectionService.getPendingRequests(userId);
        return ResponseEntity.ok(connections);
    }


    /**
     * Get sent requests
     * GET /api/community/connections/sent
     */
    @GetMapping("/sent")
    public ResponseEntity<List<ConnectionResponseDTO>> getSentRequests (@RequestHeader("X-User-Id") Long userId) {
        List<ConnectionResponseDTO> connections = connectionService.getSentRequests(userId);
        return ResponseEntity.ok(connections);
    }


    /**
     * Count connections
     * GET /api/community/connections/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countConnections(@RequestHeader("X-User-Id") Long userId) {
        long count = connectionService.countConnections(userId);
        return ResponseEntity.ok(count);
    }


    /**
     * Check if connected
     * GET /api/community/connections/check/{otherUserId}
     */
    @GetMapping("/check/{otherUserId}")
    public ResponseEntity<Boolean> areConnected(
            @PathVariable Long otherUserId,
            @RequestHeader("X-User-Id") Long userId ) {
        boolean connected = connectionService.areConnected(userId, otherUserId);
        return ResponseEntity.ok(connected);
    }

}

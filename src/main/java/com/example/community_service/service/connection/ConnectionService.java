package com.example.community_service.service.connection;

import com.example.community_service.entity.connection.ConnectionStatus;
import com.example.community_service.dto.connection.ConnectionResponseDTO;
import com.example.community_service.dto.connection.SendConnectionRequestDTO;
import com.example.community_service.entity.connection.Connection;
import com.example.community_service.exception.ConnectionAlreadyExistsException;
import com.example.community_service.exception.ConnectionNotFoundException;
import com.example.community_service.exception.InvalidConnectionException;
import com.example.community_service.exception.UnauthorizedException;
import com.example.community_service.repository.connection.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionService {
    private final ConnectionRepository connectionRepository;

    /**
     * Send connection request
     */
    public ConnectionResponseDTO sendConnectionRequest(SendConnectionRequestDTO request){
        // Check if users are same
        if(request.getSenderId().equals(request.getReceiverId())){
            throw new InvalidConnectionException("Cannot connect with yourself");
        }

        // Check if connection already exists
        if(connectionRepository.existsConnectionBetweenUsers(request.getSenderId(), request.getReceiverId())){
            throw new ConnectionAlreadyExistsException("Connection already exists");
        }

        Connection connection = Connection.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Connection savedConnection = connectionRepository.save(connection);

        return mapToResponse(savedConnection);

    }

    /**
     * Accept connection request
     */
     public ConnectionResponseDTO acceptConnection(Long connectionId, Long userId) {
         Connection connection = connectionRepository.findById(connectionId)
                 .orElseThrow(() -> new ConnectionNotFoundException("Connection not found: " + connectionId));

         // Verify Receiver
         if(!connection.getReceiverId().equals(userId)){
             throw new UnauthorizedException("Not authorize to except this exception");
         }

         // Check if already accepted
         if(connection.getStatus() == ConnectionStatus.ACCEPTED) {
             throw new InvalidConnectionException("Connection already accepted");
         }

         connection.setStatus(ConnectionStatus.ACCEPTED);
         connection.setAcceptedAt(LocalDateTime.now());

         Connection updatedConnection = connectionRepository.save(connection);

         return mapToResponse(updatedConnection);
     }


    /**
     * Reject connection request
     */
    public void rejectConnection(Long connectionId, Long userId){
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ConnectionNotFoundException("Connection not found: " + connectionId));

        // Verify Receiver
        if(!connection.getReceiverId().equals(userId)){
            throw new UnauthorizedException("Not authorized to reject this connection");
        }

        connection.setStatus(ConnectionStatus.REJECTED);
        connectionRepository.save(connection);
    }


    /**
     * Remove connection
     */
    public void removeConnection(Long connectionId, Long userId){
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ConnectionNotFoundException("Connection not found: " + connectionId));

        // Verify ownership (either sender or receiver can remove)
        if(!connection.getSenderId().equals(userId) && !connection.getReceiverId().equals(userId)){
            throw new UnauthorizedException("Not authorized to remove this connection.");
        }

        connectionRepository.delete(connection);
    }


    /**
     * Get user's connections
     */
    public List<ConnectionResponseDTO> getUserConnections(Long userId){
        List<Connection> connections = connectionRepository.findByUserId(userId);
        return connections.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    /**
     * Get accepted connections
     */
    public List<ConnectionResponseDTO> getAcceptedConnections(Long userId){
        List<Connection> connections = connectionRepository.findByUserIdAndStatus(userId,ConnectionStatus.ACCEPTED);
        return connections.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    /**
     * Get pending requests (received)
     */
    public List<ConnectionResponseDTO> getPendingRequests(Long userId){
        List<Connection> connections = connectionRepository.findByReceiverIdAndStatus(userId, ConnectionStatus.PENDING);
        return connections.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    /**
     * Get sent requests
     */
    public List<ConnectionResponseDTO>  getSentRequests(Long userId){
        List<Connection> connections = connectionRepository.findBySenderIdAndStatus(userId, ConnectionStatus.PENDING);
        return connections.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    /**
     * Count accepted connections
     */
    public long countConnections(Long userId){
        return connectionRepository.countAcceptedConnections(userId);
    }


    /**
     * Count pending requests
     */
    public long countPendingRequests(Long userId){
        return connectionRepository.countByReceiverIdAndStatus(userId, ConnectionStatus.PENDING);
    }


    /**
     * Check if connected
     */
    public boolean areConnected(Long userId1, Long userId2){
        return connectionRepository.findConnectionBetweenUsers(userId1, userId2)
                .map(connection -> connection.getStatus() == ConnectionStatus.ACCEPTED)
                .orElse(false);
    }

    // Helper method
    private ConnectionResponseDTO mapToResponse(Connection connection) {
        return ConnectionResponseDTO.builder()
                .id(connection.getId())
                .senderId(connection.getSenderId())
                .receiverId(connection.getReceiverId())
                .status(connection.getStatus())
                .createdAt(connection.getCreatedAt())
                .acceptedAt(connection.getAcceptedAt())
                .build();
    }

}

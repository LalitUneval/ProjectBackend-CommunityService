package com.example.community_service.repository.connection;

import com.example.community_service.entity.connection.ConnectionStatus;
import com.example.community_service.entity.connection.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    // Find connections where user is sender
    List<Connection> findBySenderId(Long senderId);

    // Find connections where user is receiver
    List<Connection> findByReceiverId(Long receiverId);

    // Find all connections for a user (both sent and received)
    @Query("SELECT c FROM Connection c WHERE c.senderId = :userId OR c.receiverId = :userId")
    List<Connection> findByUserId(@Param("userId") Long userId);

    // Find accepted connections for User
    //This will return all user by it status like(pending , accepted ,reject)
    @Query("SELECT c FROM Connection c WHERE (c.senderId = :userId OR c.receiverId = :userId) AND c.status = :status")
    List<Connection> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status")ConnectionStatus status);

    // Find connection between Two Users (either direction)
    @Query("""
           SELECT
                 CASE 
                     WHEN COUNT(c) > 0 
                         THEN true 
                         ELSE false 
                 END 
           FROM Connection c WHERE
           (c.senderId = :userId1 AND c.receiverId = :userId2) OR 
           (c.senderId = :userId2 AND c.receiverId = :userId1)
           """)
    Optional<Connection> findConnectionBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // Check if connection exists between Two Users
    @Query("""
          SELECT 
                CASE 
                    WHEN COUNT(c) > 0 
                        THEN true 
                        ELSE false 
                END 
                FROM Connection c WHERE 
            (c.senderId = :userId1 AND c.receiverId = :userId2) OR 
            (c.senderId = :userId2 AND c.receiverId = :userId1)
          """)
    boolean existsConnectionBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // Find connection requests received by User with status
    List<Connection> findByReceiverIdAndStatus(Long receiverId, ConnectionStatus status);

    // Find connection requests sent by User with status
    List<Connection> findBySenderIdAndStatus(Long senderId, ConnectionStatus status);

    // Count Accepted connection for a User
    @Query("SELECT COUNT(c) FROM Connection c WHERE " +
          "(c.senderId = :userId OR c.receiverId = :userId) AND c.status = 'ACCEPTED'")
    long countAcceptedConnections(@Param("userId") Long userId);

    //Count Pending requests for a User
    long countByReceiverIdAndStatus(Long receiverId, ConnectionStatus status);

    // Find connections by status
    List<Connection> findByStatus(ConnectionStatus status);


}

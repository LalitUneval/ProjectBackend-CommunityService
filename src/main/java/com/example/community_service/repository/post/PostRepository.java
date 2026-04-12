package com.example.community_service.repository.post;

import com.example.community_service.entity.community.CommunityGroup;
import com.example.community_service.entity.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Find posts by user ID
    List<Post> findByUserId(Long userId);

    // Find posts by group
    List<Post> findByGroup(CommunityGroup group);

    // Find posts by group ID (with pagination)
    Page<Post> findByGroupId(Long groupId, Pageable pageable);

    // Find posts by group ID, ordered by created date (most recent first)
    List<Post> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    // Find posts by user ID, ordered by created date
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find posts created after a certain date
    List<Post> findByCreatedAtAfter(LocalDateTime date);

    // Search posts by content (case-insensitive)
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Post> searchByContent(@Param("keyword") String keyword);

    // Find top posts by likes
    @Query("SELECT p FROM Post p ORDER BY p.likesCount DESC")
    List<Post> findTopPostsByLikes(Pageable pageable);

    // Find posts in a group with pagination (ordered by date)
    Page<Post> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    // Count posts by user ID
    long countByUserId(Long userId);

    // Count posts by group ID
    long countByGroupId(Long groupId);

    // Get recent posts across all groups (feed)
    @Query("SELECT p FROM Post p ORDER BY p.createdAt")
    Page<Post> getRecentPosts(Pageable pageable);
}


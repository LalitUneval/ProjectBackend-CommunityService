package com.example.community_service.service.post;


import com.example.community_service.dto.post.CreatePostRequestDTO;
import com.example.community_service.dto.post.PostResponseDTO;
import com.example.community_service.dto.post.UpdatePostRequestDTO;
import com.example.community_service.entity.community.CommunityGroup;
import com.example.community_service.entity.post.Post;
import com.example.community_service.exception.GroupNotFoundException;
import com.example.community_service.exception.PostNotFoundException;
import com.example.community_service.exception.UnauthorizedException;
import com.example.community_service.repository.community.CommunityRepository;
import com.example.community_service.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CommunityRepository communityGroupRepository;

    /**
     * Create post
     */
    public PostResponseDTO createPost(CreatePostRequestDTO request) {
        // Verify group exists
        CommunityGroup group = communityGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + request.getGroupId()));

        Post post = Post.builder()
                .userId(request.getUserId())
                .group(group)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .likesCount(0)
                .commentsCount(0)
                .build();

        Post savedPost = postRepository.save(post);

        return mapToResponse(savedPost);
    }


    /**
     * Get post by ID
     */
    public PostResponseDTO getPost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));

        return mapToResponse(post);

    }


    /**
     * Get posts by group (paginated)
     */
    public Page<PostResponseDTO> getGroupPosts(Long groupId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable);

        return posts.map(this::mapToResponse);

    }


    /**
     * Get user's posts
     */
    public List<PostResponseDTO> getUserPosts(Long userId) {

        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }


    /**
     * Get recent posts (feed)
     */
    public Page<PostResponseDTO> getRecentPosts(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.getRecentPosts(pageable);

        return posts.map(this::mapToResponse);

    }


    /**
     * Search posts by content
     */
    public List<PostResponseDTO> searchPosts(String keyword) {

        List<Post> posts = postRepository.searchByContent(keyword);
        return posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    /**
     * Get top posts by likes
     */
    public List<PostResponseDTO> getTopPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Post> posts = postRepository.findTopPostsByLikes(pageable);
        return posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update post
     */
    public PostResponseDTO updatePost(Long postId, UpdatePostRequestDTO request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));

        // Verify ownership
        if (!post.getUserId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to update this post");
        }

        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }

        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);

        return mapToResponse(updatedPost);
    }

    /**
     * Delete post
     */
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));

        // Verify ownership
        if (!post.getUserId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    /**
     * Increment likes count
     */
    public void incrementLikes(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));

        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    /**
     * Increment comments count
     */
    public void incrementComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));

        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
    }

    /**
     * Count user's posts
     */
    public long countUserPosts(Long userId) {
        return postRepository.countByUserId(userId);
    }

    /**
     * Count group's posts
     */
    public long countGroupPosts(Long groupId) {
        return postRepository.countByGroupId(groupId);
    }

    // Helper method
    private PostResponseDTO mapToResponse(Post post) {
        return PostResponseDTO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .groupId(post.getGroup().getId())
                .groupName(post.getGroup().getName())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .build();
    }
}

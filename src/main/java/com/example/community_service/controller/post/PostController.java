package com.example.community_service.controller.post;


import com.example.community_service.dto.post.CreatePostRequestDTO;
import com.example.community_service.dto.post.PostResponseDTO;
import com.example.community_service.dto.post.UpdatePostRequestDTO;
import com.example.community_service.service.post.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    private final PostService postService;

    /**
     * Create post
     * POST /api/community/posts
     */
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @Valid @RequestBody CreatePostRequestDTO request,
            @RequestHeader("X-User-Id") Long userId) {

        // Set userId from header
        request.setUserId(userId);
        PostResponseDTO response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get post by ID
     * GET /api/community/posts/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long postId) {
        PostResponseDTO response = postService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get posts by group (paginated)
     * GET /api/community/groups/{groupId}/posts?page=0&size=10
     * GET /api/community/posts/groups/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<PostResponseDTO>> getGroupPosts(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PostResponseDTO> posts = postService.getGroupPosts(groupId, page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get user's posts
     * GET /api/community/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponseDTO>> getUserPosts(@PathVariable Long userId) {
        List<PostResponseDTO> posts = postService.getUserPosts(userId);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get recent posts (feed)
     * GET /api/community/posts/feed?page=0&size=10
     */
    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponseDTO>> getRecentPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PostResponseDTO> posts = postService.getRecentPosts(page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * Search posts by content
     * GET /api/community/posts/search?keyword=visa
     */
    @GetMapping("/search")
    public ResponseEntity<List<PostResponseDTO>> searchPosts(@RequestParam String keyword) {
        List<PostResponseDTO> posts = postService.searchPosts(keyword);
        return ResponseEntity.ok(posts);
    }

    /**
     * Like post
     * POST /api/community/posts/{postId}/like
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId) {
        postService.incrementLikes(postId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get top posts by likes
     * GET /api/community/posts/top?limit=10
     */
    @GetMapping("/top")
    public ResponseEntity<List<PostResponseDTO>> getTopPosts(
            @RequestParam(defaultValue = "10") int limit) {

        List<PostResponseDTO> posts = postService.getTopPosts(limit);
        return ResponseEntity.ok(posts);
    }

    /**
     * Update post
     * PUT /api/community/posts/{postId}
     */
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequestDTO request,
            @RequestHeader("X-User-Id") Long userId) {

        PostResponseDTO response = postService.updatePost(postId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete post
     * DELETE /api/community/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {

        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }


}

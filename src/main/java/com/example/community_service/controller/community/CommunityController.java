package com.example.community_service.controller.community;

import com.example.community_service.dto.community.CommunityGroupRequestDTO;
import com.example.community_service.dto.community.CommunityGroupResponseDTO;
import com.example.community_service.dto.community.JoinGroupResponseDTO;
import com.example.community_service.dto.community.UpdateGroupRequestDTO;
import com.example.community_service.entity.community.CommunityGroup;
import com.example.community_service.service.community.CommunityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.community_service.entity.community.GroupMember;

@RestController
@RequestMapping("/api/community/groups")
@CrossOrigin(origins = "*")
public class CommunityController {
    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }


    /**
     * Create community group
     * POST /api/community/groups
     *
     * user can create group
     * Creator automatically becomes group admin
     */
    @PostMapping
    public ResponseEntity<?> createGroup(
            @Valid @RequestBody CommunityGroupRequestDTO request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        // Check if user has ADMIN role
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only admin users can create groups");
        }

        CommunityGroupResponseDTO response = communityService.createGroup(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Get group by ID
     * GET /api/community/groups/{groupId}
     */
    @GetMapping("/{id}")
    public  ResponseEntity<CommunityGroupResponseDTO> getGroupById(@PathVariable Long id){
        CommunityGroupResponseDTO response = communityService.getGroupById(id);
        return ResponseEntity.ok(response);
    }


    /**
     * Get all groups
     * GET /api/community/groups
     */
     @GetMapping
     public ResponseEntity<List<CommunityGroupResponseDTO>>getAllGroups(){
         List<CommunityGroupResponseDTO> groups = communityService.getAllGroups();
         return ResponseEntity.ok(groups);
     }


    /**
     * Get groups by userId which user created
     * GET /api/community/groups/my-groups
     */
    @GetMapping("/my-groups")
    public ResponseEntity<List<CommunityGroup>> getMyGroups(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(communityService.getUserGroups(userId));
    }


    /**
     * Get groups by city
     * GET /api/community/groups/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<CommunityGroupResponseDTO>> findByCity(@PathVariable String city){
        List<CommunityGroupResponseDTO> groups = communityService.findByCity(city);
        return ResponseEntity.ok(groups);
    }


    /**
     * Get groups by country
     * GET /api/community/groups/country/{country}
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<List<CommunityGroupResponseDTO>> findByCountry(@PathVariable String country) {
        List<CommunityGroupResponseDTO> groups = communityService.findByCountry(country);
        return ResponseEntity.ok(groups);
    }


    /**
     * Search groups
     * GET /api/community/groups/search?city=SF&country=India
     * use full name instead of initial characters
     */
     @GetMapping("/search")
    public ResponseEntity<List<CommunityGroupResponseDTO>> findByCityAndOriginCountry(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country ) {
         List<CommunityGroupResponseDTO> groups = communityService.findByCityAndOriginCountry(city, country);
         return ResponseEntity.ok(groups);
     }


    /**
     * Search groups by name
     * GET /api/community/groups/search-name?name=Tech
     */
    @GetMapping("/search-name")
    public ResponseEntity<List<CommunityGroupResponseDTO>> findByNameContainingIgnoreCase(@RequestParam String name) {
        List<CommunityGroupResponseDTO> groups = communityService.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(groups);
    }

    /**
     * Search groups by description
     * GET /api/community/groups/search-description?description=message
     */
    @GetMapping("/search-description")
    public ResponseEntity<List<CommunityGroupResponseDTO>> findByDescription(@RequestParam String description) {
        List<CommunityGroupResponseDTO> groups = communityService.findByDescription(description);
        return ResponseEntity.ok(groups);
    }


    /**
     * Update group
     * PUT /api/community/groups/{groupId}
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<CommunityGroupResponseDTO> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequestDTO request) {

        CommunityGroupResponseDTO response = communityService.updateGroup(groupId, request);
        return ResponseEntity.ok(response);
    }


    /**
     * Delete group (Admin only)
     * DELETE /api/community/groups/{groupId}
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long groupId,
            @RequestHeader("X-User-Role") String userRole) {

        if (!userRole.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        communityService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }


    // Join or Leave Group Endpoints

    /**
     * Join a group
     * POST /api/community/groups/{id}/join
     *
     * For PUBLIC groups: User immediately joins (status = APPROVED)
     * For PRIVATE groups: Request is sent to admin (status = PENDING)
     */
    @PostMapping("/{id}/join")
    public ResponseEntity<JoinGroupResponseDTO> joinGroup(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        GroupMember member = communityService.joinGroup(id, userId);

        JoinGroupResponseDTO response = JoinGroupResponseDTO.builder()
                .groupId(id)
                .userId(userId)
                .status(member.getStatus())
                .message(member.getStatus().equals("APPROVED")
                        ? "You have successfully joined the group!"
                        : "Join request sent! Waiting for admin approval.")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Leave a group
     * POST /api/community/groups/{id}/leave
     *
     * User can leave any group they're part of
     * After leaving, group disappears from "My Groups" and "Joined Groups" tabs
     */
    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        communityService.leaveGroup(id, userId);
        return ResponseEntity.ok().build();
    }


    /**
     * Get user's JOINED groups
     * GET /api/community/groups/my-groups/joined
     *
     * Shows groups where status = APPROVED
     * Used for "Joined Groups" tab
     */
    @GetMapping("/my-groups/joined")
    public ResponseEntity<List<CommunityGroup>> getJoinedGroups(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(communityService.getUserJoinedGroups(userId));
    }

    /**
     * Get user's PENDING groups
     * GET /api/community/groups/my-groups/pending
     *
     * Shows groups where status = PENDING (waiting for admin approval)
     * Used for "Pending Requests" tab
     */
    @GetMapping("/my-groups/pending")
    public ResponseEntity<List<CommunityGroup>> getPendingGroups(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(communityService.getUserPendingGroups(userId));
    }

    /**
     * Get user's REJECTED groups
     * GET /api/community/groups/my-groups/rejected
     *
     * Shows groups where status = REJECTED (admin rejected request)
     * Used for "Rejected" tab
     */
    @GetMapping("/my-groups/rejected")
    public ResponseEntity<List<CommunityGroup>> getRejectedGroups(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(communityService.getUserRejectedGroups(userId));
    }


    // Admin specific APIs

    /**
     * Get pending join requests for a group (Admin only)
     * GET /api/community/groups/{id}/pending-requests
     *
     * Admin sees list of users waiting for approval
     */
    @GetMapping("/{id}/pending-requests")
    public ResponseEntity<List<GroupMember>> getGroupPendingRequests(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId) {

        // Check if user is admin
        if (!communityService.isAdmin(id, adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(communityService.getPendingRequests(id));
    }

    /**
     * Approve a join request (Admin only)
     * POST /api/community/groups/{groupId}/approve/{userId}
     *
     * Admin approves user's request to join
     * User's status changes from PENDING to APPROVED
     */
    @PostMapping("/{groupId}/approve/{userId}")
    public ResponseEntity<GroupMember> approveJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long adminId) {

        GroupMember member = communityService.approveJoinRequest(groupId, userId, adminId);
        return ResponseEntity.ok(member);
    }

    /**
     * Reject a join request (Admin only)
     * POST /api/community/groups/{groupId}/reject/{userId}
     *
     * Admin rejects user's request to join
     * User's status changes from PENDING to REJECTED
     */
    @PostMapping("/{groupId}/reject/{userId}")
    public ResponseEntity<GroupMember> rejectJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long adminId) {

        GroupMember member = communityService.rejectJoinRequest(groupId, userId, adminId);
        return ResponseEntity.ok(member);
    }

    /**
     * Remove a user from group (Admin only)
     * DELETE /api/community/groups/{groupId}/members/{userId}
     *
     * Admin can remove users from the group
     * Works for both PUBLIC and PRIVATE groups
     * After removal, group disappears from user's "Joined Groups" tab
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeUserFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long adminId) {

        communityService.removeUserFromGroup(groupId, userId, adminId);
        return ResponseEntity.ok().build();
    }


    /**
     * Get all members of a group
     * GET /api/community/groups/{id}/members
     *
     * Accessible by: admin of the group
     * Returns: List of all group members with their status and role
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<?> getGroupMembers(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        if (!communityService.isMember(id, userId) &&
                !communityService.isAdmin(id, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You must be a member to view the member list");
        }

        return ResponseEntity.ok(communityService.getGroupMembers(id));
    }

    



}

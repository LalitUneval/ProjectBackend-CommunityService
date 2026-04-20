package com.example.community_service.service.community;

import com.example.community_service.dto.community.CommunityGroupRequestDTO;
import com.example.community_service.dto.community.CommunityGroupResponseDTO;
import com.example.community_service.dto.community.UpdateGroupRequestDTO;
import com.example.community_service.entity.community.CommunityGroup;
import com.example.community_service.entity.community.GroupMember;
import com.example.community_service.exception.GroupAlreadyExistsException;
import com.example.community_service.exception.GroupNotFoundException;
import com.example.community_service.repository.community.CommunityRepository;
import com.example.community_service.repository.community.GroupMemberRepository;
import com.example.community_service.service.chat.ChatService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * - Added ChatService to broadcast JOIN/LEAVE messages when membership changes
 * - approveJoinRequest now broadcasts "User joined" to the group topic
 * - leaveGroup now broadcasts "User left" to the group topic
 *
 * @Lazy on ChatService breaks the circular dependency:
 * CommunityService → ChatService
 */
@Service
@Transactional
public class CommunityService {

    private final CommunityRepository repository;
    private final GroupMemberRepository groupMemberRepository;
    private final ChatService chatService;

    public CommunityService(
            CommunityRepository repository,
            GroupMemberRepository groupMemberRepository,
            @Lazy ChatService chatService) {
        this.repository = repository;
        this.groupMemberRepository = groupMemberRepository;
        this.chatService = chatService;
    }



    // Group CRUD
    @Caching(evict = {
            @CacheEvict(value = "allGroups", allEntries = true),
            @CacheEvict(value = "groupsByCity", allEntries = true),
            @CacheEvict(value = "groupsByCountry", allEntries = true),
            @CacheEvict(value = "groupSearch", allEntries = true)
    })
    public CommunityGroupResponseDTO createGroup(CommunityGroupRequestDTO request, Long userId) {
        if (repository.existsByName(request.getName())) {
            throw new GroupAlreadyExistsException("Group already exists: " + request.getName());
        }

        CommunityGroup group = CommunityGroup.builder()
                .name(request.getName())
                .city(request.getCity())
                .originCountry(request.getOriginCountry())
                .description(request.getDescription())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .createdBy(userId)
                .adminName(request.getAdminName())
                .build();

        CommunityGroup savedGroup = repository.save(group);

        GroupMember adminMember = GroupMember.builder()
                .groupId(savedGroup.getId())
                .userId(userId)
                .status("APPROVED")
                .role("ADMIN")
                .joinedAt(LocalDateTime.now())
                .build();

        groupMemberRepository.save(adminMember);

        return maptoResponse(savedGroup);
    }

    @Cacheable(value = "groupById", key = "#id")
    public CommunityGroupResponseDTO getGroupById(Long id) {
        CommunityGroup group = repository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException("Group not Found: " + id));
        return maptoResponse(group);
    }

    @Cacheable(value = "allGroups", key = "'all'")
    public List<CommunityGroupResponseDTO> getAllGroups() {
        return repository.findAll().stream()
                .map(this::maptoResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "groupsByCity", key = "#city")
    public List<CommunityGroupResponseDTO> findByCity(String city) {
        return repository.findByCity(city).stream()
                .map(this::maptoResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "groupsByCountry", key = "#country")
    public List<CommunityGroupResponseDTO> findByCountry(String country) {
        return repository.findByOriginCountry(country).stream()
                .map(this::maptoResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "groupsByCityAndCountry", key = "#city + '-' + #country")
    public List<CommunityGroupResponseDTO> findByCityAndOriginCountry(String city, String country) {
        return repository.findByCityAndOriginCountry(city, country).stream()
                .map(this::maptoResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "groupsByName", key = "#name")
    public List<CommunityGroupResponseDTO> findByNameContainingIgnoreCase(String name) {
        return repository.findByNameContainingIgnoreCase(name).stream()
                .map(this::maptoResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "groupsByDescription", key = "#description")
    public List<CommunityGroupResponseDTO> findByDescription(String description) {
        return repository.findByDescription(description).stream()
                .map(this::maptoResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "groupSearch", key = "#city + '-' + #country")
    public List<CommunityGroupResponseDTO> searchGroups(String city, String country) {
        return repository.searchGroups(city, country).stream()
                .map(this::maptoResponse)
                .collect(Collectors.toList());
    }


    @Caching(
            put = { @CachePut(value = "groupById", key = "#id") },
            evict = {
                    @CacheEvict(value = "allGroups", allEntries = true),
                    @CacheEvict(value = "groupsByCity", allEntries = true),
                    @CacheEvict(value = "groupsByCountry", allEntries = true),
                    @CacheEvict(value = "groupSearch", allEntries = true),
                    @CacheEvict(value = "groupsByName", allEntries = true)
            }
    )
    public CommunityGroupResponseDTO updateGroup(Long id, UpdateGroupRequestDTO request) {
        CommunityGroup group = repository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException("Group not Found: " + id));

        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }

        return maptoResponse(repository.save(group));
    }

    @Caching(evict = {
            @CacheEvict(value = "groupById", key = "#id"),
            @CacheEvict(value = "allGroups", allEntries = true),
            @CacheEvict(value = "groupsByCity", allEntries = true),
            @CacheEvict(value = "groupsByCountry", allEntries = true),
            @CacheEvict(value = "groupSearch", allEntries = true),
            @CacheEvict(value = "userGroups", allEntries = true)
    })
    public void deleteGroup(Long id) {
        if (!repository.existsById(id)) {
            throw new GroupNotFoundException("Group not Found: " + id);
        }
        repository.deleteById(id);
    }


    // Membership — with chat notifications
    @Caching(evict = {
            @CacheEvict(value = "userGroups", key = "#userId"),
            @CacheEvict(value = "groupById", key = "#groupId")
    })
    public GroupMember joinGroup(Long groupId, Long userId) {
        CommunityGroup group = repository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        var existing = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        GroupMember member = GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .status(group.getIsPublic() ? "APPROVED" : "PENDING")
                .role("MEMBER")
                .build();

        if (group.getIsPublic()) {
            member.setJoinedAt(LocalDateTime.now());
            groupMemberRepository.save(member);

            // Broadcast Join message to group chat (public groups have  immediate join)
            chatService.broadcastJoinMessage(groupId, userId, "User " + userId);
            return member;
        }

        return groupMemberRepository.save(member);
    }

    /**
     * Updated: Broadcasts a LEAVE message to the group chat when user leaves.
     */
    @Caching(evict = {
            @CacheEvict(value = "userGroups", key = "#userId"),
            @CacheEvict(value = "groupById", key = "#groupId")
    })
    public void leaveGroup(Long groupId, Long userId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .ifPresent(member -> {
                    groupMemberRepository.delete(member);
                    // Notify group that this user left
                    chatService.broadcastLeaveMessage(groupId, userId, "User " + userId);
                });
    }

    public List<GroupMember> getPendingRequests(Long groupId) {
        return groupMemberRepository.findByGroupIdAndStatus(groupId, "PENDING");
    }

    // admin only controls

    /**
     * Updated: Broadcasts a JOIN message to the group chat when admin approves -> for private group only.
     */
    @Caching(evict = {
            @CacheEvict(value = "userGroups", key = "#userId"),
            @CacheEvict(value = "groupById", key = "#groupId")
    })
    public GroupMember approveJoinRequest(Long groupId, Long userId, Long adminId) {
        var adminMember = groupMemberRepository.findByGroupIdAndUserId(groupId, adminId)
                .orElseThrow(() -> new RuntimeException("Not authorized"));

        if (!"ADMIN".equals(adminMember.getRole())) {
            throw new RuntimeException("Only admin can approve requests");
        }

        var member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        member.setStatus("APPROVED");
        member.setJoinedAt(LocalDateTime.now());
        GroupMember saved = groupMemberRepository.save(member);

        //  Notify group that this user was approved and joined
        chatService.broadcastJoinMessage(groupId, userId, "User " + userId);

        return saved;
    }

    public GroupMember rejectJoinRequest(Long groupId, Long userId, Long adminId) {
        var adminMember = groupMemberRepository.findByGroupIdAndUserId(groupId, adminId)
                .orElseThrow(() -> new RuntimeException("Not authorized"));

        if (!"ADMIN".equals(adminMember.getRole())) {
            throw new RuntimeException("Only admin can reject requests");
        }

        var member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        member.setStatus("REJECTED");
        return groupMemberRepository.save(member);
    }

    @CacheEvict(value = "userGroups", key = "#userId")
    public void removeUserFromGroup(Long groupId, Long userId, Long adminId) {
        var adminMember = groupMemberRepository.findByGroupIdAndUserId(groupId, adminId)
                .orElseThrow(() -> new RuntimeException("Not authorized"));

        if (!"ADMIN".equals(adminMember.getRole())) {
            throw new RuntimeException("Only admin can remove users");
        }

        var targetMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("ADMIN".equals(targetMember.getRole())) {
            throw new RuntimeException("Cannot remove admin");
        }

        groupMemberRepository.delete(targetMember);

        // Notify group that this user was removed
        chatService.broadcastLeaveMessage(groupId, userId, "User " + userId);
    }


    // Access Checked
    public boolean isMemberOrAdmin(Long groupId, Long userId) {
        return isMember(groupId, userId) || isAdmin(groupId, userId);
    }

    public List<GroupMember> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    public boolean isMember(Long groupId, Long userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(m -> "APPROVED".equals(m.getStatus()))
                .orElse(false);
    }

    public boolean isAdmin(Long groupId, Long userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(m -> "ADMIN".equals(m.getRole()))
                .orElse(false);
    }

    @Cacheable(value = "userGroups", key = "#userId")
    public List<CommunityGroup> getUserGroups(Long userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .filter(m -> "APPROVED".equals(m.getStatus()))
                .map(m -> repository.findById(m.getGroupId()).orElse(null))
                .filter(g -> g != null)
                .toList();
    }

    public List<CommunityGroup> getUserPendingGroups(Long userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .filter(m -> "PENDING".equals(m.getStatus()))
                .map(m -> repository.findById(m.getGroupId()).orElse(null))
                .filter(g -> g != null)
                .toList();
    }

    public List<CommunityGroup> getUserRejectedGroups(Long userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .filter(m -> "REJECTED".equals(m.getStatus()))
                .map(m -> repository.findById(m.getGroupId()).orElse(null))
                .filter(g -> g != null)
                .toList();
    }

    public List<CommunityGroup> getUserJoinedGroups(Long userId) {
        return getUserGroups(userId);
    }


    // Mapping

    private CommunityGroupResponseDTO maptoResponse(CommunityGroup group) {
        CommunityGroupResponseDTO dto = new CommunityGroupResponseDTO();
        dto.setId(group.getId());
        dto.setCreatedBy(group.getCreatedBy());
        dto.setName(group.getName());
        dto.setCity(group.getCity());
        dto.setOriginCountry(group.getOriginCountry());
        dto.setDescription(group.getDescription());
        dto.setIsPublic(group.getIsPublic());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setAdminName(group.getAdminName());
        return dto;
    }

//    private String getUserName(Long userId) {
//        return "Admin User"; // temporary — replace with Feign call to user-service
//    }
}
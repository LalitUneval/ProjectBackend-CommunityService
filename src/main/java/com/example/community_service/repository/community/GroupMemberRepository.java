package com.example.community_service.repository.community;

import com.example.community_service.entity.community.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // find by group id
    List<GroupMember> findByGroupId(Long groupId);

    // find by user id
    List<GroupMember> findByUserId(Long userId);

    // find by group id and status
    List<GroupMember> findByGroupIdAndStatus(Long groupId, String status);

    // find by group id and user id
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
}

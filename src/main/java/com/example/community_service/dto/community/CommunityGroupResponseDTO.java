package com.example.community_service.dto.community;

import com.example.community_service.dto.post.PostResponseDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunityGroupResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String city;
    private String originCountry;
    private String description;
    private Boolean isPublic;
    private String adminName;
    private LocalDateTime createdAt;
    private Long createdBy; // Admin id

}

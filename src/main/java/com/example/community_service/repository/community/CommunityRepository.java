package com.example.community_service.repository.community;


import com.example.community_service.entity.community.CommunityGroup;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<CommunityGroup, Long> {

    // Find groups by city
    List<CommunityGroup> findByCity(String city);

    // Find group by origin country
    List<CommunityGroup> findByOriginCountry(String originCountry);

    // Find groups by city and origin country
    List<CommunityGroup> findByCityAndOriginCountry(String city, String country);

    // Find groups by name
    List<CommunityGroup> findByNameContainingIgnoreCase(String name);

    // Find by description
    List<CommunityGroup> findByDescription(String description);

    // Check if its group exist or not
    boolean existsByName(@NotBlank(message = "Group name is required") String name);

    // Find all groups with filters
    @Query("SELECT g FROM CommunityGroup g WHERE " +
            "(:city IS NULL OR g.city = :city) AND " +
            "(:originCountry IS NULL OR g.originCountry = :originCountry)")
    List<CommunityGroup> searchGroups(@Param("city") String city,
                                      @Param("originCountry") String originCountry);

}

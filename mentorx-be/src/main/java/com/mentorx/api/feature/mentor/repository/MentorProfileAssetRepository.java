package com.mentorx.api.feature.mentor.repository;

import com.mentorx.api.feature.mentor.entity.MentorProfileAsset;
import com.mentorx.api.feature.mentor.enums.MentorProfileAssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorProfileAssetRepository extends JpaRepository<MentorProfileAsset, UUID> {

    List<MentorProfileAsset> findByMentorProfileIdOrderByDisplayOrderAscCreatedAtDesc(UUID mentorProfileId);

    List<MentorProfileAsset> findByMentorProfileIdAndTypeOrderByDisplayOrderAscCreatedAtDesc(
            UUID mentorProfileId,
            MentorProfileAssetType type
    );
}

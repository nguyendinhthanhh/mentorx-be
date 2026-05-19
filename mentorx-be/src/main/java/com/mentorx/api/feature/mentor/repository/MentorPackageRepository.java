package com.mentorx.api.feature.mentor.repository;

import com.mentorx.api.feature.mentor.entity.MentorPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorPackageRepository extends JpaRepository<MentorPackage, UUID> {

    List<MentorPackage> findByMentorProfileIdOrderByDisplayOrderAsc(UUID mentorProfileId);

    List<MentorPackage> findByMentorProfileIdAndIsActiveTrueOrderByDisplayOrderAsc(UUID mentorProfileId);
}

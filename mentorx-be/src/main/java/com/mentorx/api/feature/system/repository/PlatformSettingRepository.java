package com.mentorx.api.feature.system.repository;

import com.mentorx.api.feature.system.entity.PlatformSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, String> {
}

package com.nhom12.enggo_backend.repository.gamification;

import com.nhom12.enggo_backend.entity.gamification.MissionBadge;
import com.nhom12.enggo_backend.entity.gamification.MissionBadgeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionBadgeRepository extends JpaRepository<MissionBadge, MissionBadgeId> {
}

package com.nhom12.enggo_backend.repository.gamification;

import com.nhom12.enggo_backend.entity.gamification.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Integer> {
}

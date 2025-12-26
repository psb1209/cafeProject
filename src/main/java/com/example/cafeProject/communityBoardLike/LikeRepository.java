package com.example.cafeProject.communityBoardLike;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like,Integer> {
    Optional<Like> findByCommunityBoardNumberAndUserId(int communityNumberId,int userId);
}

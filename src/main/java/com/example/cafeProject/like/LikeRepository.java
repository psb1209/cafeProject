package com.example.cafeProject.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like,Integer> {
    Optional<Like> findByCommunityBoardNumberAndUserId(int communityNumberId,int userId);
    Optional<Like> findByInformationBoardNumberAndUserId(int informationNumberId,int userId);
    Optional<Like> findByNoticeBoardNumberAndUserId(int noticeNumberId,int userId);
    Optional<Like> findByOperationBoardNumberAndUserId(int operationNumberId,int userId);

}

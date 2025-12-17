package com.example.cafeProject.communityBoardComment;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityBoardCommentRepository extends JpaRepository<CommunityBoardComment,Integer> {
    Page<CommunityBoardComment> findByCommunityBoardId(int communityBoardId, Pageable pageable);


    List<CommunityBoardComment> findByCommunityBoardId(int CommunityBoardId);
}

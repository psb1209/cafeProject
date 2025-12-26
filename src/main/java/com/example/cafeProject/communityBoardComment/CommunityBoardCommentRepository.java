package com.example.cafeProject.communityBoardComment;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityBoardCommentRepository extends JpaRepository<CommunityBoardComment,Integer> {
    Page<CommunityBoardComment> findByCommunityBoardIdOrderByRefDescLevelAsc(int communityBoardId, Pageable pageable);
    //댓글 목록 Ref/level기준으로 order by정렬 메소드

    //ref최대값 찾는 메소드
    @Query("SELECT COALESCE(MAX(c.ref),0) FROM CommunityBoardComment c") int getMaxRef();
    
    
    //level순차 누적 메소드
    @Modifying
    @Query("UPDATE CommunityBoardComment c SET c.level=c.level + 1 WHERE c.ref = :ref AND c.level >:level")
    int updateRelevel(@Param("ref") int ref, @Param("level") int level);




    List<CommunityBoardComment> findByCommunityBoardId(int CommunityBoardId);
}

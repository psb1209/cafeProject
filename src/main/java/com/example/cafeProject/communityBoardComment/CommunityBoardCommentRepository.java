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
    //댓글목록 페이징 달기위함
    //부모글 id번호를 찾는것(코멘트 디비에도 부모글 id가 저장되어있기때문에 포함된 엔티티를 가져옴)
    @Query("SELECT COALESCE(MAX(c.ref),0) FROM CommunityBoardComment c") int getMaxRef();

    @Modifying
    @Query("UPDATE CommunityBoardComment c SET c.level=c.level + 1 WHERE c.ref = :ref AND c.level >:level")
    int updateRelevel(@Param("ref") int ref, @Param("level") int level);




    List<CommunityBoardComment> findByCommunityBoardId(int CommunityBoardId);
}

package com.example.cafeProject.communityBoard;

import com.example.cafeProject.operationBoard.OperationBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityBoardRepository extends JpaRepository<CommunityBoard, Integer> {
    /** 게시글 검색 */
    @Query("""
            select b
            from OperationBoard b
            where lower(b.subject) like lower(concat('%', :keyword, '%'))
    """)
    Page<CommunityBoard> searchBySubject(@Param("keyword") String keyword, Pageable pageable);

    /*=============================== 각 게시판 공지글 ===================================*/
    List<CommunityBoard> findBySubNoticeTrueOrderByCreateDateDesc();

    Page<CommunityBoard> findBySubNoticeFalse(Pageable pageable);
    /*=====================================================================================*/
}

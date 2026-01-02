package com.example.cafeProject.operationBoard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OperationBoardRepository extends JpaRepository<OperationBoard, Integer> {
    /** 게시글 검색 */
    @Query("""
            select b
            from OperationBoard b
            where lower(b.subject) like lower(concat('%', :keyword, '%'))
    """)
    Page<OperationBoard> searchBySubject(@Param("keyword") String keyword, Pageable pageable);

    /*=============================== 각 게시판 공지글 ===================================*/
    List<OperationBoard> findBySubNoticeTrueOrderByCreateDateDesc();

    Page<OperationBoard> findBySubNoticeFalse(Pageable pageable);
    /*=====================================================================================*/
}

package com.example.cafeProject.informationBoard;

import com.example.cafeProject.operationBoard.OperationBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InformationBoardRepository extends JpaRepository<InformationBoard, Integer> {
    /** 게시글 검색 */
    @Query("""
            select b
            from InformationBoard b
            where lower(b.subject) like lower(concat('%', :keyword, '%'))
    """)
    Page<InformationBoard> searchBySubject(@Param("keyword") String keyword, Pageable pageable);
}

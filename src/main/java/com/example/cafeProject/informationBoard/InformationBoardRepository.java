package com.example.cafeProject.informationBoard;

import com.example.cafeProject.operationBoard.OperationBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InformationBoardRepository extends JpaRepository<InformationBoard, Integer> {
    /** 게시글 검색 */
    @Query("""
            select b
            from InformationBoard b
            where lower(b.subject) like lower(concat('%', :keyword, '%'))
    """)
    Page<InformationBoard> searchBySubject(@Param("keyword") String keyword, Pageable pageable);

    /*=============================== 각 게시판 공지글 ===================================*/
    List<InformationBoard> findBySubNoticeTrueOrderByCreateDateDesc(); //SubNotice필드의 값이 True인 레코드들을 최신등록일순으로 가져와라 / 공지글은 페이징 적용 X

    Page<InformationBoard> findBySubNoticeFalse(Pageable pageable); //공지글이 아닌 레코드들은 페이징 적용
    /*=====================================================================================*/
}

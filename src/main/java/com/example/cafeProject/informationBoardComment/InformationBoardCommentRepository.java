package com.example.cafeProject.informationBoardComment;

import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InformationBoardCommentRepository extends JpaRepository<InformationBoardComment, Integer> {

    Page<InformationBoardComment> findByInformationBoardIdOrderByRefDescLevelAsc(int informationBoardId, Pageable pageable);

    List<InformationBoardComment> findByInformationBoardId(int informationBoardId);

    /*============================================== 대댓글 ===============================================*/
    //ref최대값 찾는 메소드 (부모글 정렬)
    @Query("SELECT COALESCE(MAX(c.ref),0) FROM InformationBoardComment c") int getMaxRef();


    //level순차 누적 메소드 (자식글 정렬)
    @Modifying
    @Query("UPDATE InformationBoardComment c SET c.level=c.level + 1 WHERE c.ref = :ref AND c.level >:level")
    int updateRelevel(@Param("ref") int ref, @Param("level") int level);

    /*@Query("SELECT  FROM InformationBoardComment c") int a();*/


    /*============================================== 대댓글 ===============================================*/
}

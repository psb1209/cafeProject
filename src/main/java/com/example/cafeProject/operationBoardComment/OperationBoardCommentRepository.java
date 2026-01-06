package com.example.cafeProject.operationBoardComment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OperationBoardCommentRepository extends JpaRepository<OperationBoardComment, Integer> {

    Page<OperationBoardComment> findByOperationBoardIdOrderByRefDescLevelAsc(int operationBoardId, Pageable pageable);

    List<OperationBoardComment> findByOperationBoardId(int operationBoardId);

    /*============================================== 대댓글 ===============================================*/
    //ref최대값 찾는 메소드 (부모글 정렬)
    @Query("SELECT COALESCE(MAX(c.ref),0) FROM OperationBoardComment c") int getMaxRef();
    

    //level순차 누적 메소드 (자식글 정렬)
    @Modifying
    @Query("UPDATE OperationBoardComment c SET c.level=c.level + 1 WHERE c.ref = :ref AND c.level >:level")
    int updateRelevel(@Param("ref") int ref, @Param("level") int level);

    /*@Query("SELECT  FROM OperationBoardComment c") int a();*/


    /*============================================== 대댓글 ===============================================*/

}

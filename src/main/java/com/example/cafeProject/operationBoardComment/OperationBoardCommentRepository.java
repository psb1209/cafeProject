package com.example.cafeProject.operationBoardComment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OperationBoardCommentRepository extends JpaRepository<OperationBoardComment, Integer> {

    Page<OperationBoardComment> findByOperationBoardId(int operationBoardId, Pageable pageable);

    List<OperationBoardComment> findByOperationBoardId(int operationBoardId);

/*    @Query("select count(:operationBoardId) from operationBoardComment")
    public void countComment(int operationBoardId);*/
}

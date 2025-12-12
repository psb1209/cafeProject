package com.example.cafeProject.operationBoardComment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationBoardCommentRepository extends JpaRepository<OperationBoardComment, Integer> {

    Page<OperationBoardComment> findByOperationBoardId(int operationBoardId, Pageable pageable);
}

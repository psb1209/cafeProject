package com.example.cafeProject.informationBoardComment;

import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformationBoardCommentRepository extends JpaRepository<InformationBoardComment, Integer> {

    Page<InformationBoardComment> findByInformationBoard_Id(int informationBoardId, Pageable pageable);
}

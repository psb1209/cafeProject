package com.example.cafeProject.noticeBoardComment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeBoardCommentRepository extends JpaRepository<NoticeBoardComment, Integer> {
    //Page<NoticeBoardComment> findByNoticeBoardId(int noticeBoardId, Pageable pageable);
    Page<NoticeBoardComment> findByNoticeBoardId(int noticeBoardId, Pageable pageable);
}

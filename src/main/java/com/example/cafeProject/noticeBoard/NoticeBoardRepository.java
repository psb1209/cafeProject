package com.example.cafeProject.noticeBoard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeBoardRepository extends JpaRepository<NoticeBoard, Integer> {
    /** 게시글 검색 */
    @Query("""
            select b
            from NoticeBoard b
            where lower(b.subject) like lower(concat('%', :keyword, '%'))
    """)
    Page<NoticeBoard> searchBySubject(@Param("keyword") String keyword, Pageable pageable);
}

package com.example.cafeProject.noticeBoard2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeBoard2Repository extends JpaRepository<NoticeBoard2, Integer> {
    /** 게시글 검색 */
    @Query("""
            select b
            from NoticeBoard2 b
            where lower(b.subject) like lower(concat('%', :keyword, '%'))
    """)
    Page<NoticeBoard2> searchBySubject(@Param("keyword") String keyword, Pageable pageable);
}

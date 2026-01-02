/*
package com.example.cafeProject.noticeBoard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeBoardRepository extends JpaRepository<NoticeBoard, Integer> {
    // 댓글수 카운트
    @Query("SELECT COUNT(*) FROM NoticeBoardComment WHERE noticeBoard.id = :noticeBoardId")
    int countCommentsByNoticeBoardId(@Param("noticeBoardId") int noticeBoardId);

//    @Query("SELECT COUNT(*) FROM NoticeBoard WHERE noticeBoard.id = :noticeBoardId")
//    int countByNoticeBoardId(@Param("noticeBoardId") int noticeBoardId);
}
*/

package com.example.cafeProject.noticeBoardComment2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeBoardComment2Repository extends JpaRepository<NoticeBoardComment2, Integer> {

    Page<NoticeBoardComment2> findByNoticeBoard2_IdOrderByRefDescLevelAsc(int noticeBoardId, Pageable pageable);

    List<NoticeBoardComment2> findByNoticeBoard2_Id(int noticeBoardId);

    //ref최대값 찾는 메소드
    @Query("SELECT COALESCE(MAX(c.ref),0) FROM NoticeBoardComment2 c") int getMaxRef();


    //level순차 누적 메소드
    @Modifying
    @Query("UPDATE NoticeBoardComment2 c SET c.level=c.level + 1 WHERE c.ref = :ref AND c.level >:level")
    int updateRelevel(@Param("ref") int ref, @Param("level") int level);


}

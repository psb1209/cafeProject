package com.example.cafeProject.noticeBoardComment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeBoardCommentRepository extends JpaRepository<NoticeBoardComment, Integer> {
    Page<NoticeBoardComment> findByNoticeBoardId(int noticeBoardId, Pageable pageable);

    List<NoticeBoardComment> findByNoticeBoardId(int noticeBoardId);

//    @Query("select coalesce(max(nbc.ref), 0) from noticeBoardComment nbc")
//    int getMaxRef();
//
//    @Modifying
//    @Query("UPDATE noticeBoardComment nbc SET nbc.level = nbc.level + 1 WHERE nbc.ref = :ref AND nbc.level > :level")
//    void updateRelevel(@Param("ref") int ref, @Param("level") int level);
}

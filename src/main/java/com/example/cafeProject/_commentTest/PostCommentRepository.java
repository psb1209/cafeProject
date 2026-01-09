package com.example.cafeProject._commentTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {

    Page<PostComment> findByPostIdOrderByRefDescLevelAsc(int postId, Pageable pageable);

    List<PostComment> findByPostId(int postId);

    // ref 최대값
    @Query("SELECT COALESCE(MAX(c.ref),0) FROM PostComment c")
    int getMaxRef();

    // level 순차 누적
    @Modifying
    @Query("UPDATE PostComment c SET c.level=c.level + 1 WHERE c.ref = :ref AND c.level > :level")
    int updateRelevel(@Param("ref") int ref, @Param("level") int level);
}

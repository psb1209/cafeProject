package com.example.cafeProject._commentTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {

    /**
     * 댓글/대댓글 전체 목록(삭제 댓글 포함)
     * - 댓글 최신순(ref desc)
     * - 대댓글 등록순(level asc)
     */
    Page<PostComment> findByPostIdOrderByRefDescLevelAsc(int postId, Pageable pageable);

    /** 삭제되지 않은 댓글만 */
    Page<PostComment> findByPostIdAndDeletedFalseOrderByRefDescLevelAsc(int postId, Pageable pageable);

    List<PostComment> findByPostId(int postId);

    List<PostComment> findByPostIdAndDeletedFalse(int postId);

    long countByPostIdAndDeletedFalse(int postId);

    // ref 최대값
    @Query("SELECT COALESCE(MAX(c.ref),0) FROM PostComment c")
    int getMaxRef();

    // 현재 ref(댓글 묶음)에서 가장 큰 level (대댓글 등록순 정렬용)
    @Query("SELECT COALESCE(MAX(c.level),0) FROM PostComment c WHERE c.ref = :ref")
    int getMaxLevelInRef(@Param("ref") int ref);

    // level 순차 누적
    @Modifying
    @Query("UPDATE PostComment c SET c.level=c.level + 1 WHERE c.ref = :ref AND c.level > :level")
    int updateRelevel(@Param("ref") int ref, @Param("level") int level);
}

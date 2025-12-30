/*
package com.example.cafeProject.board_view;

import com.example.cafeProject.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface Board_viewRepository extends JpaRepository<Like,Integer> {
    Optional<Like> findByCommunityBoardNumberAndUserId(int communityNumberId,int userId);
    Optional<Like> findByInformationBoardNumberAndUserId(int informationNumberId,int userId);
    Optional<Like> findByNoticeBoardNumberAndUserId(int noticeNumberId,int userId);
    Optional<Like> findByOperationBoardNumberAndUserId(int operationNumberId,int userId);

    @Query("""
            SELECT COUNT(*)
            FROM Like l
            WHERE l.communityBoardNumber = :id
    """)
    int countLikeWithCommunityBoardNumber(@Param("id") int communityNumberId);

    @Query("""
            SELECT COUNT(*)
            FROM Like l
            WHERE l.informationBoardNumber = :id
    """)
    int countLikeWithInformationBoardNumber(@Param("id") int InformationNumberId);

    @Query("""
            SELECT COUNT(*)
            FROM Like l
            WHERE l.noticeBoardNumber = :id
    """)
    int countLikeWithNoticeBoardNumber(@Param("id") int NoticeNumberId);

    @Query("""
            SELECT COUNT(*)
            FROM Like l
            WHERE l.operationBoardNumber = :id
    """)
    int countLikeWithOperationBoardNumber(@Param("id") int OperationNumberId);
}
*/

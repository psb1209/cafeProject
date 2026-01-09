package com.example.cafeProject.board_view;

import com.example.cafeProject.operationBoard.OperationBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface Board_viewRepository extends JpaRepository<Board_view, Integer> {

    // ======================
    // 중복 조회 체크 (유저 + 게시글)
    // ======================
    Optional<Board_view> findByCommunityBoardNumberAndUserId(int boardId, int userId);
    Optional<Board_view> findByNoticeBoardNumberAndUserId(int boardId, int userId);
    Optional<Board_view> findByInformationBoardNumberAndUserId(int boardId, int userId);
    Optional<Board_view> findByOperationBoardNumberAndUserId(int boardId, int userId);
    Optional<Board_view> findByPostIdAndUserId(int postId, int userId);

    // ======================
    // 조회수 COUNT
    // ======================
    @Query("""
        SELECT COUNT(l)
        FROM Board_view l
        WHERE l.operationBoardNumber = :id
    """)
    int countBoard_viewWithOperationBoardNumber(@Param("id") int id);

    @Query("""
        SELECT COUNT(l)
        FROM Board_view l
        WHERE l.communityBoardNumber = :id
    """)
    int countBoard_viewWithCommunityBoardNumber(@Param("id") int id);

    @Query("""
        SELECT COUNT(l)
        FROM Board_view l
        WHERE l.noticeBoardNumber = :id
    """)
    int countBoard_viewWithNoticeBoardNumber(@Param("id") int id);

    @Query("""
        SELECT COUNT(l)
        FROM Board_view l
        WHERE l.informationBoardNumber = :id
    """)
    int countBoard_viewWithInformationBoardNumber(@Param("id") int id);

    @Query("""
        SELECT COUNT(l)
        FROM Board_view l
        WHERE l.postId = :id
    """)
    int countBoard_viewWithPostId(@Param("id") int id);

}

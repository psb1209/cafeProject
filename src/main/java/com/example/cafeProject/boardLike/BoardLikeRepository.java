package com.example.cafeProject.boardLike;

import com.example.cafeProject.noticeBoard.NoticeBoardDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Integer> {
    // 좋아요 개수
    int countByBoardTypeAndBoardId(BoardType boardType, int boardId);

    // 좋아요 여부
    boolean existsByBoardTypeAndBoardIdAndMemberId(
            BoardType boardType, int boardId, int memberId);

    // 좋아요 취소
    void deleteByBoardTypeAndBoardIdAndMemberId(
            BoardType boardType, int boardId, int memberId);

    // 게시글 삭제 시 좋아요 전체 삭제
    void deleteByBoardTypeAndBoardId(BoardType boardType, int boardId);
}

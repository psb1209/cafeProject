package com.example.cafeProject.boardLike;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoard.NoticeBoard;
import com.example.cafeProject.noticeBoard.NoticeBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardLikeService {

    private final BoardLikeRepository boardLikeRepository;
    private final MemberService memberService;

    // 좋아요 토글
    public void toggle(BoardType boardType, int boardId, Authentication authentication) {

        if (memberService.isNotLogin(authentication)) throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");

        Member member = memberService.viewCurrentMember(authentication);

        boolean exists =
                boardLikeRepository.existsByBoardTypeAndBoardIdAndMemberId(boardType, boardId, member.getId());

        // 이미 좋아요가 눌려있다면 다시 눌렀을때 좋아요 취소
        if (exists) {
            boardLikeRepository.deleteByBoardTypeAndBoardIdAndMemberId(boardType, boardId, member.getId());
            return;
        }

        BoardLike boardLike = BoardLike.builder()
                .boardType(boardType)
                .boardId(boardId)
                .member(member)
                .build();

        boardLikeRepository.save(boardLike);
    }

    // 해당 게시글의 좋아요 수를 카운트
    @Transactional(readOnly = true)
    public int getLikeCount(BoardType boardType, int boardId) {
        return boardLikeRepository.countByBoardTypeAndBoardId(boardType, boardId);
    }

    // 해당 게시글의 좋아요를 특정 회원이 이미 눌렀는지 안눌렀는지 확인
    @Transactional(readOnly = true)
    public boolean isLiked(BoardType boardType, int boardId, int memberId) {
        return boardLikeRepository.existsByBoardTypeAndBoardIdAndMemberId(
                boardType, boardId, memberId);
    }

    @Transactional(readOnly = true)
    public void deleteByBoard(BoardType boardType, int boardId) {
        boardLikeRepository.deleteByBoardTypeAndBoardId(boardType, boardId);
    }
}


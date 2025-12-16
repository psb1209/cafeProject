package com.example.cafeProject.noticeBoardComment;


import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoard.NoticeBoard;
import com.example.cafeProject.noticeBoard.NoticeBoardDTO;
import com.example.cafeProject.noticeBoard.NoticeBoardRepository;
import com.example.cafeProject.operationBoard.OperationBoardDTO;
import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class NoticeBoardCommentService {
    private final NoticeBoardCommentRepository noticeBoardCommentRepository;
    private final NoticeBoardRepository noticeBoardRepository;
    private final MemberService memberService;

    public Page<NoticeBoardComment> listByNoticeBoard(int noticeBoardId, Pageable pageable) {
        return noticeBoardCommentRepository.findByNoticeBoardId(noticeBoardId, pageable);
    }

    public NoticeBoardComment view(int noticeBoardCommentId) {
        NoticeBoardComment noticeBoardComment = null;
        Optional<NoticeBoardComment> optionalNoticeBoardComment = noticeBoardCommentRepository.findById(noticeBoardCommentId);
        if (optionalNoticeBoardComment.isPresent()) {
            noticeBoardComment = optionalNoticeBoardComment.get();
        }

        return noticeBoardComment;
    }

    public void createProc(NoticeBoardCommentDTO noticeBoardCommentDTO) {
        NoticeBoard noticeBoard = null;
        Optional<NoticeBoard> optionalNoticeBoard = noticeBoardRepository.findById(noticeBoardCommentDTO.getNoticeBoardId());
        if (optionalNoticeBoard.isPresent()) {
            noticeBoard = optionalNoticeBoard.get();
            NoticeBoardComment noticeBoardComment = new NoticeBoardComment();
            noticeBoardComment.setContent(noticeBoardCommentDTO.getContent());
            noticeBoardComment.setNoticeBoard(noticeBoard);
            noticeBoardComment.setMember(memberService.view(noticeBoardCommentDTO.getMemberId()));
            noticeBoardCommentRepository.save(noticeBoardComment);

        } else {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }

    }

    public void deleteProc(int noticeBoardCommentId) {
        NoticeBoardComment noticeBoardComment = new NoticeBoardComment();
        noticeBoardComment.setId(noticeBoardCommentId);
        noticeBoardCommentRepository.delete(noticeBoardComment);
    }


    public void setDeleteAll(NoticeBoardDTO noticeBoardDTO) {
        List<NoticeBoardComment> noticeBoardComment = noticeBoardCommentRepository.findByNoticeBoardId(noticeBoardDTO.getId());

        noticeBoardCommentRepository.deleteAll(noticeBoardComment);
    }

//    public void replyProc() {
//        int ref = noticeBoardCommentRepository.getMaxRef() + 1;
//        noticeBoardCommentRepository.updateRelevel(3, 5);
//    }
}

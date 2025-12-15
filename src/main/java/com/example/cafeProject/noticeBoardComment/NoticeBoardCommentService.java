package com.example.cafeProject.noticeBoardComment;


import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoard.NoticeBoard;
import com.example.cafeProject.noticeBoard.NoticeBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

//        NoticeBoard noticeBoard = null;
//        Optional<Member> optionalMember = memberRepository.findById(paramDTO.getMemberId());
//        if (optionalMember.isPresent()) {
//            member = optionalMember.get();
//        }
//
//        Comment comment = new Comment();
//        comment.setContent(paramDTO.getContent());
//        comment.setMember(member);
//        comment.setGuestBook(guestBook);
//
//        commentRepository.save(comment);
    }

    public void deleteProc(int noticeBoardCommentId) {
        NoticeBoardComment noticeBoardComment = new NoticeBoardComment();
        noticeBoardComment.setId(noticeBoardCommentId);
        noticeBoardCommentRepository.delete(noticeBoardComment);
    }
}

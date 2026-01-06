package com.example.cafeProject.noticeBoardComment;

import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoard.NoticeBoard;
import com.example.cafeProject.noticeBoard.NoticeBoardDTO;
import com.example.cafeProject.noticeBoard.NoticeBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final MemberRepository memberRepository;

    @Transactional
    public boolean setInsert(NoticeBoardCommentDTO noticeBoardCommentDTO, User user) {
        NoticeBoard noticeBoard = getSelectOneById_noticeBoard(noticeBoardCommentDTO.getNoticeBoardId()); //카페 게시글 정보 확인
        Member member = getSelectOneById_member(user.getUsername()); //로그인한 사용자가 카페회원이 맞는지 (인증:아이디,비번확인 + 인가:권한확인)

        Grade oldGrade = member.getGrade(); //로그인한 사용자의 예전 등급

        noticeBoardCommentDTO.setRef(noticeBoardCommentRepository.getMaxRef() + 1);
        noticeBoardCommentDTO.setStep(0);
        noticeBoardCommentDTO.setLevel(0);
        NoticeBoardComment noticeBoardComment = NoticeBoardComment.dtoToEntity(noticeBoardCommentDTO, member, noticeBoard);
        noticeBoardCommentRepository.save(noticeBoardComment);

        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = noticeBoardComment.getMember().getGrade();//새로운 등급
        return oldGrade != newGrade; //등급이 바뀌었으면 true 반환
    }

    //게시글 기본키로 게시글 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public NoticeBoard getSelectOneById_noticeBoard(int id) {
        return noticeBoardRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 게시글 없음"));
    }

    //아이디로 맴버 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public Member getSelectOneById_member(String username) {
        return memberRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("해당 맴버 없음"));
    }
    
    @Transactional
    public Page<NoticeBoardComment> getCommentListPage(int noticeBoardId, Pageable pageable) {
        return noticeBoardCommentRepository.findByNoticeBoardIdOrderByRefDescLevelAsc(noticeBoardId, pageable);
    }

    @Transactional
    public void setDelete(NoticeBoardCommentDTO paramDTO) {
        NoticeBoardComment noticeBoardComment = noticeBoardCommentRepository.findById(paramDTO.getNoticeBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        noticeBoardCommentRepository.delete(noticeBoardComment);
    }

    @Transactional
    public void setDeleteAll(NoticeBoardDTO paramDTO) {
        List<NoticeBoardComment> noticeBoardComment = noticeBoardCommentRepository.findByNoticeBoardId(paramDTO.getId());

        noticeBoardCommentRepository.deleteAll(noticeBoardComment);
    }

    @Transactional
    public NoticeBoardComment setUpdate(NoticeBoardCommentDTO paramDTO) {
        NoticeBoardComment noticeBoardComment = noticeBoardCommentRepository.findById(paramDTO.getNoticeBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        noticeBoardComment.setContent(paramDTO.getContent());
        return noticeBoardComment;
    }

    public NoticeBoardComment getNoticeBoardCommentId(NoticeBoardCommentDTO paramDTO) {
        return noticeBoardCommentRepository.findById(paramDTO.getNoticeBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));
    }

    //대댓글 추가
    @Transactional
    public void replysetInsert(
            NoticeBoardCommentDTO paramDTO,
            UserDetails userDetails
    ){
        NoticeBoardComment noticeBoardComment_ = noticeBoardCommentRepository.findById(paramDTO.getNoticeBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 없음"));

        noticeBoardCommentRepository.updateRelevel(
                noticeBoardComment_.getRef(),
                noticeBoardComment_.getLevel()
        );

        int ref=noticeBoardComment_.getRef();
        int step=noticeBoardComment_.getStep()+1;
        int level=noticeBoardComment_.getLevel()+1;

        NoticeBoard noticeBoard=null;
        Optional<NoticeBoard> noticeBoardOptional=noticeBoardRepository.findById(paramDTO.getNoticeBoardId());
        if(noticeBoardOptional.isPresent()){
            noticeBoard=noticeBoardOptional.get(); //부모글 존재유무판단
        }


        NoticeBoardComment noticeBoardComment=new NoticeBoardComment();
        noticeBoardComment.setContent(paramDTO.getContent());
        noticeBoardComment.setNoticeBoard(noticeBoard);
        noticeBoardComment.setMember(member);
        noticeBoardComment.setRef(ref);
        noticeBoardComment.setStep(step);
        noticeBoardComment.setLevel(level);

        noticeBoardCommentRepository.save(noticeBoardComment);
    }

    //회원등업
    public Member updateGrade(Member member) {
        member.increaseReplyCount(); //댓글작성 +1
        int posts = member.getPostCount();
        int replies = member.getReplyCount();

        if (posts >= 7 && replies >= 12) member.setGrade(Grade.SPECIAL);
        else if (posts >= 5 && replies >= 10) member.setGrade(Grade.BEST);
        else if (posts >= 3 && replies >= 5) member.setGrade(Grade.REGULAR);
        else member.setGrade(Grade.USER);
        return member;
    }


}




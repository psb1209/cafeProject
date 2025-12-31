package com.example.cafeProject.noticeBoardComment2;

import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoard2.NoticeBoard2;
import com.example.cafeProject.noticeBoard2.NoticeBoard2DTO;
import com.example.cafeProject.noticeBoard2.NoticeBoard2Repository;
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
public class NoticeBoardComment2Service {

    private final NoticeBoardComment2Repository noticeBoardComment2Repository;
    private final NoticeBoard2Repository noticeBoard2Repository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Transactional
    public boolean setInsert(NoticeBoardComment2DTO noticeBoardComment2DTO, User user) {
        NoticeBoard2 noticeBoard2 = getSelectOneById_noticeBoard2(noticeBoardComment2DTO.getNoticeBoardId()); //카페 게시글 정보 확인
        Member member = getSelectOneById_member(user.getUsername()); //로그인한 사용자가 카페회원이 맞는지 (인증:아이디,비번확인 + 인가:권한확인)

        Grade oldGrade = member.getGrade(); //로그인한 사용자의 예전 등급

        NoticeBoardComment2 noticeBoardComment2 = NoticeBoardComment2.dtoToEntity(noticeBoardComment2DTO, member, noticeBoard2);
        noticeBoardComment2Repository.save(noticeBoardComment2);

        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = noticeBoardComment2.getMember().getGrade();//새로운 등급
        return oldGrade != newGrade; //등급이 바뀌었으면 true 반환
    }

    //게시글 기본키로 게시글 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public NoticeBoard2 getSelectOneById_noticeBoard2(int id) {
        return noticeBoard2Repository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 게시글 없음"));
    }

    //아이디로 맴버 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public Member getSelectOneById_member(String username) {
        return memberRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("해당 맴버 없음"));
    }
    
    @Transactional
    public Page<NoticeBoardComment2> getCommentListPage(int noticeBoardId, Pageable pageable) {
        return noticeBoardComment2Repository.findByNoticeBoard2_IdOrderByRefDescLevelAsc(noticeBoardId, pageable);
    }

    @Transactional
    public void setDelete(NoticeBoardComment2DTO paramDTO) {
        NoticeBoardComment2 noticeBoardComment2 = noticeBoardComment2Repository.findById(paramDTO.getNoticeBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        noticeBoardComment2Repository.delete(noticeBoardComment2);
    }

    @Transactional
    public void setDeleteAll(NoticeBoard2DTO paramDTO) {
        List<NoticeBoardComment2> noticeBoardComment2 = noticeBoardComment2Repository.findByNoticeBoard2_Id(paramDTO.getId());

        noticeBoardComment2Repository.deleteAll(noticeBoardComment2);
    }

    @Transactional
    public NoticeBoardComment2 setUpdate(NoticeBoardComment2DTO paramDTO) {
        NoticeBoardComment2 noticeBoardComment2 = noticeBoardComment2Repository.findById(paramDTO.getNoticeBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        noticeBoardComment2.setContent(paramDTO.getContent());
        return noticeBoardComment2;
    }

    public NoticeBoardComment2 getNoticeBoardCommentId(NoticeBoardComment2DTO paramDTO) {
        return noticeBoardComment2Repository.findById(paramDTO.getNoticeBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));
    }

    //대댓글 추가
    @Transactional
    public void replysetInsert(
            NoticeBoardComment2DTO paramDTO,
            UserDetails userDetails
    ){
        NoticeBoardComment2 noticeBoardComment2_ = noticeBoardComment2Repository.findById(paramDTO.getNoticeBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 없음"));

        noticeBoardComment2Repository.updateRelevel(
                noticeBoardComment2_.getRef(),
                noticeBoardComment2_.getLevel()
        );

        int ref=noticeBoardComment2_.getRef();
        int step=noticeBoardComment2_.getStep()+1;
        int level=noticeBoardComment2_.getLevel()+1;

        NoticeBoard2 noticeBoard2=null;
        Optional<NoticeBoard2> noticeBoard2Optional=noticeBoard2Repository.findById(paramDTO.getNoticeBoardId());
        if(noticeBoard2Optional.isPresent()){
            noticeBoard2=noticeBoard2Optional.get(); //부모글 존재유무판단
        }


        NoticeBoardComment2 noticeBoardComment2=new NoticeBoardComment2();
        noticeBoardComment2.setContent(paramDTO.getContent());
        noticeBoardComment2.setNoticeBoard2(noticeBoard2);
        noticeBoardComment2.setMember(member);
        noticeBoardComment2.setRef(ref);
        noticeBoardComment2.setStep(step);
        noticeBoardComment2.setLevel(level);

        noticeBoardComment2Repository.save(noticeBoardComment2);
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




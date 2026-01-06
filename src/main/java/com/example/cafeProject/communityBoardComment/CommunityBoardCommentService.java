package com.example.cafeProject.communityBoardComment;

import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.communityBoard.CommunityBoard;
import com.example.cafeProject.communityBoard.CommunityBoardDTO;
import com.example.cafeProject.communityBoard.CommunityBoardRepository;
import com.example.cafeProject.communityBoardComment.CommunityBoardComment;
import com.example.cafeProject.communityBoardComment.CommunityBoardCommentDTO;
import com.example.cafeProject.communityBoardComment.CommunityBoardCommentRepository;

import com.example.cafeProject.communityBoard.CommunityBoard;
import com.example.cafeProject.communityBoardComment.CommunityBoardComment;
import com.example.cafeProject.communityBoardComment.CommunityBoardCommentDTO;

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
public class CommunityBoardCommentService {

    private final CommunityBoardCommentRepository communityBoardCommentRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Transactional
    public boolean setInsert(CommunityBoardCommentDTO communityBoardCommentDTO, User user) {
        CommunityBoard communityBoard = getSelectOneById_communityBoard(communityBoardCommentDTO.getCommunityBoardId()); //카페 게시글 정보 확인
        Member member = memberService.viewCurrentMember(user); //로그인한 사용자가 카페회원이 맞는지 (인증:아이디,비번확인 + 인가:권한확인)

        Grade oldGrade = member.getGrade(); //로그인한 사용자의 예전 등급

        /*============================================== 대댓글 추가사항===============================================*/
        // ref, step, level값 DTO에 담기
        communityBoardCommentDTO.setRef(communityBoardCommentRepository.getMaxRef() + 1);
        communityBoardCommentDTO.setStep(0);
        communityBoardCommentDTO.setLevel(0);
        /*============================================== 대댓글 추가사항===============================================*/
        CommunityBoardComment communityBoardComment = CommunityBoardComment.dtoToEntity(communityBoardCommentDTO, member, communityBoard);
        communityBoardCommentRepository.save(communityBoardComment);

        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = communityBoardComment.getMember().getGrade();//새로운 등급
        return oldGrade != newGrade; //등급이 바뀌었으면 true 반환
    }

    //게시글 기본키로 게시글 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public CommunityBoard getSelectOneById_communityBoard(int id) {
        return communityBoardRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 게시글 없음"));
    }
    
    @Transactional
    public Page<CommunityBoardComment> getCommentListPage(int communityBoardId, Pageable pageable) {
        return communityBoardCommentRepository.findByCommunityBoardIdOrderByRefDescLevelAsc(communityBoardId, pageable);
    }

    @Transactional
    public void setDelete(CommunityBoardCommentDTO paramDTO) {
        CommunityBoardComment communityBoardComment = communityBoardCommentRepository.findById(paramDTO.getCommunityBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        communityBoardCommentRepository.delete(communityBoardComment);
    }

    @Transactional
    public void setDeleteAll(CommunityBoardDTO paramDTO) {
        List<CommunityBoardComment> communityBoardComment = communityBoardCommentRepository.findByCommunityBoardId(paramDTO.getId());

        communityBoardCommentRepository.deleteAll(communityBoardComment);
    }

    @Transactional
    public CommunityBoardComment setUpdate(CommunityBoardCommentDTO paramDTO) {
        CommunityBoardComment communityBoardComment = communityBoardCommentRepository.findById(paramDTO.getCommunityBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        communityBoardComment.setContent(paramDTO.getContent());
        return communityBoardComment;
    }

    public CommunityBoardComment getCommunityBoardCommentId(CommunityBoardCommentDTO paramDTO) {
        return communityBoardCommentRepository.findById(paramDTO.getCommunityBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));
    }

    /*============================================== 대댓글 ===============================================*/
    //대댓글 추가
    @Transactional
    public void replySetInsert(
            CommunityBoardCommentDTO paramDTO,
            UserDetails userDetails
    ){
        // 게시글 유무 확인
        CommunityBoard communityBoard = communityBoardRepository.findById(paramDTO.getCommunityBoardId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 부모글 유무 확인
        CommunityBoardComment communityBoardComment_ = communityBoardCommentRepository.findById(paramDTO.getCommunityBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 로그인 유무 확인
        Member member = memberService.viewOptional(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 자식글 정렬
        communityBoardCommentRepository.updateRelevel(
                communityBoardComment_.getRef(),
                communityBoardComment_.getLevel()
        );

        int ref = communityBoardComment_.getRef();
        int step = communityBoardComment_.getStep() + 1;
        int level = communityBoardComment_.getLevel() + 1;

        CommunityBoardComment communityBoardComment = new CommunityBoardComment();
        communityBoardComment.setContent(paramDTO.getContent());
        communityBoardComment.setCommunityBoard(communityBoard);
        communityBoardComment.setMember(member);
        communityBoardComment.setRef(ref);
        communityBoardComment.setStep(step);
        communityBoardComment.setLevel(level);

        communityBoardCommentRepository.save(communityBoardComment);
    }
    /*============================================== 대댓글 ===============================================*/

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




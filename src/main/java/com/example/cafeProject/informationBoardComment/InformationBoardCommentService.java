package com.example.cafeProject.informationBoardComment;

import com.example.cafeProject.informationBoard.InformationBoard;
import com.example.cafeProject.informationBoard.InformationBoardDTO;
import com.example.cafeProject.informationBoard.InformationBoardRepository;
import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import com.example.cafeProject.member.RoleType;
import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import com.example.cafeProject.operationBoardComment.OperationBoardCommentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class InformationBoardCommentService {

    private final InformationBoardCommentRepository informationBoardCommentRepository;
    private final InformationBoardRepository informationBoardRepository;
    private final MemberRepository memberRepository;


    //댓글 기본키로 댓글 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public InformationBoardComment getSelectOneById(int id) {
        return informationBoardCommentRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 댓글 없음"));
    }

    //게시글 기본키로 게시글 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public InformationBoard getSelectOneById_informationBoard(int id) {
        return informationBoardRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 게시글 없음"));
    }

    //아이디로 맴버 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public Member getSelectOneById_member(String username) {
        return memberRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("해당 맴버 없음"));
    }

    //댓글 페이징
    @Transactional(readOnly = true)
    public Page<InformationBoardComment> getSelectAllPage(int id, Pageable pageable) {
        return informationBoardCommentRepository.findByInformationBoard_Id(id, pageable);
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


    //카페회원만 댓글 입력
    @Transactional
    public boolean setInsert(InformationBoardCommentDTO informationBoardCommentDTO, User user) {
        InformationBoard informationBoard = getSelectOneById_informationBoard(informationBoardCommentDTO.getInformationBoardId()); //카페 게시글 정보 확인
        Member member = getSelectOneById_member(user.getUsername()); //로그인한 사용자가 카페회원이 맞는지 (인증:아이디,비번확인 + 인가:권한확인)

        Grade oldGrade = member.getGrade(); //로그인한 사용자의 예전 등급

        InformationBoardComment informationBoardComment = InformationBoardComment.dtoToEntity(informationBoardCommentDTO, member, informationBoard);
        informationBoardCommentRepository.save(informationBoardComment);
        
        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = informationBoardComment.getMember().getGrade();//새로운 등급
        return oldGrade != newGrade; //등급이 바뀌었으면 true 반환
    }


    //작성자만 댓글 수정
    @Transactional
    public InformationBoardComment setUpdate(InformationBoardCommentDTO informationBoardCommentDTO, User user) {
        InformationBoardComment informationBoardComment = informationBoardCommentRepository.findById(informationBoardCommentDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        informationBoardComment.setContent(informationBoardCommentDTO.getContent());
        return informationBoardComment;
    }


    //작성자 & 관리자 & 매니저만 댓글 삭제
    @Transactional
    public void setDelete(InformationBoardCommentDTO informationBoardCommentDTO, User user) {

        InformationBoardComment informationBoardComment = getSelectOneById(informationBoardCommentDTO.getId());
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        boolean isAdminOrManager = false;
        for(GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")||role.equals("ROLE_MANAGER")) {
                isAdminOrManager = true;
                break;
            }
        }
        boolean isAuthor = user.getUsername().equals(informationBoardComment.getMember().getUsername());

        if(isAuthor||isAdminOrManager) {
            informationBoardCommentRepository.delete(informationBoardComment);
        } else {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
    }




}


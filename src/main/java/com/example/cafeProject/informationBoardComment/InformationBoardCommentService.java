package com.example.cafeProject.informationBoardComment;

import com.example.cafeProject.informationBoard.InformationBoard;
import com.example.cafeProject.informationBoard.InformationBoardRepository;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import lombok.RequiredArgsConstructor;
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
    public InformationBoardComment getSelectOneById(int id) {
        return informationBoardCommentRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 댓글 없음"));
    }

    //게시글 기본키로 게시글 레코드 한줄 찾기
    public InformationBoard getSelectOneById_informationBoard(int id) {
        return informationBoardRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 게시글 없음"));
    }

    //아이디로 맴버 레코드 한줄 찾기
    public Member getSelectOneById_member(String username) {
        return memberRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("해당 맴버 없음"));
    }


    //카페회원만 댓글 입력
    @Transactional
    public void setInsert(InformationBoardCommentDTO informationBoardCommentDTO, User user) {
        InformationBoard informationBoard = getSelectOneById_informationBoard(informationBoardCommentDTO.getInformationBoardId());
        Member member = getSelectOneById_member(user.getUsername());
        InformationBoardComment informationBoardComment = InformationBoardComment.dtoToEntity(informationBoardCommentDTO, member, informationBoard);
        informationBoardCommentRepository.save(informationBoardComment);
    }

    //작성자만 댓글 수정
    @Transactional
    public void setUpdate(InformationBoardCommentDTO informationBoardCommentDTO, User user) {

        InformationBoardComment informationBoardComment = getSelectOneById(informationBoardCommentDTO.getId());
        if(!user.getUsername().equals(informationBoardComment.getMember().getUsername())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        informationBoardComment.setContent(informationBoardCommentDTO.getContent());
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


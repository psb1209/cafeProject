package com.example.cafeProject.informationBoard;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
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
public class InformationBoardService {

    private final InformationBoardRepository informationBoardRepository;
    private final MemberRepository memberRepository;


    //기본키로 정보게시글 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public InformationBoard getSelectOneById(int id) {
        return informationBoardRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글 없음"));
    }

    //아이디로 맴버 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public Member getSelectOneById_member(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(()-> new IllegalArgumentException("해당 맴버 없음"));
    }

    //기본키로 맴버 레코드 한줄 찾기 --> 메서드 오버라이딩
    @Transactional(readOnly = true)
    public Member getSelectOneById_member(int id) {
        return memberRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 맴버 없음"));
    }

    //목록 페이징 기능
    @Transactional(readOnly = true)
    public Page<InformationBoard> getSelectAllPage(Pageable pageable) {
        return informationBoardRepository.findAll(pageable);
    }

    //카페 회원만 게시글 작성
    @Transactional
    public void setInsert(InformationBoardDTO informationBoardDTO, User user) {

       Member member = getSelectOneById_member(user.getUsername());
       InformationBoard informationBoard = InformationBoard.dtoToEntity(informationBoardDTO, member);
       informationBoardRepository.save(informationBoard);
    }

    //작성자만 게시글 수정
    @Transactional
    public void setUpdate(InformationBoardDTO informationBoardDTO, User user) {

        InformationBoard informationBoard = getSelectOneById(informationBoardDTO.getId());
        if(!user.getUsername().equals(informationBoard.getMember().getUsername())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        informationBoard.setSubject(informationBoardDTO.getSubject());
        informationBoard.setContent(informationBoardDTO.getContent());
        //수정일 추가??
    }

    //조회수 증가
    @Transactional
    public InformationBoard increaseViewCount(int id) {
        InformationBoard informationBoard = getSelectOneById(id);
        informationBoard.IncreaseViewCnt();
        return informationBoard;
    }

    //작성자 & 관리자 & 매니저만 게시글 삭제
    @Transactional
    public void setDelete(InformationBoardDTO informationBoardDTO, User user) {

        Member member_principal = getSelectOneById_member(user.getUsername());
        InformationBoard informationBoard = getSelectOneById(informationBoardDTO.getId());
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        boolean isAdminOrManager = false;
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER")) {
                isAdminOrManager = true;
                break;
            }
        }
        boolean isAuthor = user.getUsername().equals(informationBoard.getMember().getUsername());

        if (isAuthor || isAdminOrManager) {
            informationBoardRepository.delete(informationBoard);
        } else {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
    }






}

package com.example.cafeProject.informationBoard;

import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import com.example.cafeProject.member.RoleType;
import com.example.cafeProject.operationBoard.OperationBoard;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    //목록 페이징 기능 + 키워드 검색 기능
    @Transactional(readOnly = true)
    public Page<InformationBoard> getSelectAllPage(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) // 검색을 안 했을 경우
            return informationBoardRepository.findAll(pageable);

        return informationBoardRepository.searchBySubject(keyword.trim(), pageable);
    }


    //회원등업
    @Transactional
    public void updateGrade(Member member) {

        member.increasePostCount(); //게시글 작성 +1
        int posts = member.getPostCount();
        int replies = member.getReplyCount();

        if (posts >= 7 && replies >= 12) member.setGrade(Grade.SPECIAL);
        else if (posts >= 5 && replies >= 10) member.setGrade(Grade.BEST);
        else if (posts >= 3 && replies >= 5) member.setGrade(Grade.REGULAR);
        else member.setGrade(Grade.USER);
    }

    //카페 회원만 게시글 작성
    @Transactional
    public boolean setInsert(InformationBoardDTO informationBoardDTO, User user) {
       Member member = getSelectOneById_member(user.getUsername());
       Grade oldGrade = member.getGrade(); //예전 등급

       InformationBoard informationBoard = InformationBoard.dtoToEntity(informationBoardDTO, member);
       informationBoardRepository.save(informationBoard);

        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = informationBoard.getMember().getGrade(); //새로운 등급
        return oldGrade != newGrade; //비교해서 등급이 바뀌었으면 true 반환
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

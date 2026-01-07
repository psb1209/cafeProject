package com.example.cafeProject.informationBoardComment;

import com.example.cafeProject.informationBoard.InformationBoard;
import com.example.cafeProject.informationBoard.InformationBoardDTO;
import com.example.cafeProject.informationBoard.InformationBoardRepository;
import com.example.cafeProject.member.*;
import com.example.cafeProject.operationBoard.OperationBoard;
import com.example.cafeProject.operationBoard.OperationBoardDTO;
import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import com.example.cafeProject.operationBoardComment.OperationBoardCommentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class InformationBoardCommentService {

    private final InformationBoardCommentRepository informationBoardCommentRepository;
    private final InformationBoardRepository informationBoardRepository;
    private final MemberService memberService;


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

    //댓글 페이징
    @Transactional
    public Page<InformationBoardComment> getCommentListPage(int informationBoardId, Pageable pageable) {
        return informationBoardCommentRepository.findByInformationBoardIdOrderByRefDescLevelAsc(informationBoardId, pageable);
    }


    //카페회원만 댓글 입력
    @Transactional
    public boolean setInsert(InformationBoardCommentDTO informationBoardCommentDTO, User user) {
        InformationBoard informationBoard = getSelectOneById_informationBoard(informationBoardCommentDTO.getInformationBoardId()); //카페 게시글 정보 확인
        Member member = memberService.viewCurrentMember(user); //로그인한 사용자가 카페회원이 맞는지 (인증:아이디,비번확인 + 인가:권한확인)

        Grade oldGrade = member.getGrade(); //로그인한 사용자의 예전 등급

        /*============================================== 대댓글 추가사항===============================================*/
        // ref, step, level값 DTO에 담기
        informationBoardCommentDTO.setRef(informationBoardCommentRepository.getMaxRef() + 1);
        informationBoardCommentDTO.setStep(0);
        informationBoardCommentDTO.setLevel(0);
        /*============================================== 대댓글 추가사항===============================================*/

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

    @Transactional
    public void setDeleteAll(InformationBoardDTO informationBoardDTO) {
        List<InformationBoardComment> informationBoardComment = informationBoardCommentRepository.findByInformationBoardId(informationBoardDTO.getId());

        informationBoardCommentRepository.deleteAll(informationBoardComment);
    }

    //회원등업
    @Transactional
    public void updateGrade(Member member) {

        //관리자는 회원등급에 영향을 받지 않도록.
        if (member.getRole() == RoleType.ADMIN || member.getRole() == RoleType.MANAGER) {
            return;
        }

        member.increaseReplyCount(); //댓글작성 +1
        int posts = member.getPostCount();
        int replies = member.getReplyCount();

        if (posts >= 7 && replies >= 12) member.setGrade(Grade.SPECIAL);
        else if (posts >= 5 && replies >= 10) member.setGrade(Grade.BEST);
        else if (posts >= 3 && replies >= 5) member.setGrade(Grade.REGULAR);
        else member.setGrade(Grade.USER);

    }

    /*============================================== 대댓글 ===============================================*/
    //대댓글 추가
    @Transactional
    public void replySetInsert(
            InformationBoardCommentDTO informationBoardCommentDTO,
            UserDetails userDetails
    ){
        // 게시글 유무 확인
        InformationBoard informationBoard = informationBoardRepository.findById(informationBoardCommentDTO.getInformationBoardId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 부모글 유무 확인
        InformationBoardComment informationBoardComment_ = informationBoardCommentRepository.findById(informationBoardCommentDTO.getInformationBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 로그인 유무 확인
        Member member = memberService.viewOptional(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 자식글 정렬
        informationBoardCommentRepository.updateRelevel(
                informationBoardComment_.getRef(),
                informationBoardComment_.getLevel()
        );

        int ref = informationBoardComment_.getRef();
        int step = informationBoardComment_.getStep() + 1;
        int level = informationBoardComment_.getLevel() + 1;

        InformationBoardComment informationBoardComment = new InformationBoardComment();
        informationBoardComment.setContent(informationBoardCommentDTO.getContent());
        informationBoardComment.setInformationBoard(informationBoard);
        informationBoardComment.setMember(member);
        informationBoardComment.setRef(ref);
        informationBoardComment.setStep(step);
        informationBoardComment.setLevel(level);

        informationBoardCommentRepository.save(informationBoardComment);
    }
    /*============================================== 대댓글 ===============================================*/



}


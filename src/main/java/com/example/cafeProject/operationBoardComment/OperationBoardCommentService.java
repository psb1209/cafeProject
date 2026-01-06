package com.example.cafeProject.operationBoardComment;

import com.example.cafeProject.operationBoard.OperationBoard;
import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.MemberRepository;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.operationBoard.OperationBoardDTO;
import com.example.cafeProject.operationBoard.OperationBoardRepository;
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
public class OperationBoardCommentService {

    private final OperationBoardCommentRepository operationBoardCommentRepository;
    private final OperationBoardRepository operationBoardRepository;
    private final MemberService memberService;

    @Transactional
    public boolean setInsert(OperationBoardCommentDTO operationBoardCommentDTO, User user) {
        OperationBoard operationBoard = getSelectOneById_operationBoard(operationBoardCommentDTO.getOperationBoardId()); //카페 게시글 정보 확인
        Member member = memberService.viewCurrentMember(user); //로그인한 사용자가 카페회원이 맞는지 (인증:아이디,비번확인 + 인가:권한확인)

        Grade oldGrade = member.getGrade(); //로그인한 사용자의 예전 등급
        
        /*============================================== 대댓글 추가사항===============================================*/
        // ref, step, level값 DTO에 담기
        operationBoardCommentDTO.setRef(operationBoardCommentRepository.getMaxRef() + 1);
        operationBoardCommentDTO.setStep(0);
        operationBoardCommentDTO.setLevel(0);
        /*============================================== 대댓글 추가사항===============================================*/
        OperationBoardComment operationBoardComment = OperationBoardComment.dtoToEntity(operationBoardCommentDTO, member, operationBoard);
        operationBoardCommentRepository.save(operationBoardComment);

        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = operationBoardComment.getMember().getGrade();//새로운 등급
        return oldGrade != newGrade; //등급이 바뀌었으면 true 반환
    }

    //게시글 기본키로 게시글 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public OperationBoard getSelectOneById_operationBoard(int id) {
        return operationBoardRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 게시글 없음"));
    }
    
    @Transactional
    public Page<OperationBoardComment> getCommentListPage(int operationBoardId, Pageable pageable) {
        return operationBoardCommentRepository.findByOperationBoardIdOrderByRefDescLevelAsc(operationBoardId, pageable);
    }

    @Transactional
    public void setDelete(OperationBoardCommentDTO paramDTO) {
        OperationBoardComment operationBoardComment = operationBoardCommentRepository.findById(paramDTO.getOperationBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        operationBoardCommentRepository.delete(operationBoardComment);
    }

    @Transactional
    public void setDeleteAll(OperationBoardDTO paramDTO) {
        List<OperationBoardComment> operationBoardComment = operationBoardCommentRepository.findByOperationBoardId(paramDTO.getId());

        operationBoardCommentRepository.deleteAll(operationBoardComment);
    }

    @Transactional
    public OperationBoardComment setUpdate(OperationBoardCommentDTO paramDTO) {
        OperationBoardComment operationBoardComment = operationBoardCommentRepository.findById(paramDTO.getOperationBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        operationBoardComment.setContent(paramDTO.getContent());
        return operationBoardComment;
    }

    public OperationBoardComment getOperationBoardCommentId(OperationBoardCommentDTO paramDTO) {
        return operationBoardCommentRepository.findById(paramDTO.getOperationBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));
    }

    /*============================================== 대댓글 ===============================================*/
    //대댓글 추가
    @Transactional
    public void replySetInsert(
            OperationBoardCommentDTO paramDTO,
            UserDetails userDetails
    ){
        // 게시글 유무 확인
        OperationBoard operationBoard = operationBoardRepository.findById(paramDTO.getOperationBoardId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 부모글 유무 확인
        OperationBoardComment operationBoardComment_ = operationBoardCommentRepository.findById(paramDTO.getOperationBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 로그인 유무 확인
        Member member = memberService.viewOptional(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 자식글 정렬
        operationBoardCommentRepository.updateRelevel(
                operationBoardComment_.getRef(),
                operationBoardComment_.getLevel()
        );
        
        int ref = operationBoardComment_.getRef();
        int step = operationBoardComment_.getStep() + 1;
        int level = operationBoardComment_.getLevel() + 1;

        OperationBoardComment operationBoardComment = new OperationBoardComment();
        operationBoardComment.setContent(paramDTO.getContent());
        operationBoardComment.setOperationBoard(operationBoard);
        operationBoardComment.setMember(member);
        operationBoardComment.setRef(ref);
        operationBoardComment.setStep(step);
        operationBoardComment.setLevel(level);

        operationBoardCommentRepository.save(operationBoardComment);
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




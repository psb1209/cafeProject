package com.example.cafeProject.operationBoardComment;

import com.example.cafeProject.communityBoardComment.CommunityBoardComment;
import com.example.cafeProject.member.MemberRepository;
import com.example.cafeProject.operationBoard.OperationBoard;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.operationBoard.OperationBoardDTO;
import com.example.cafeProject.operationBoard.OperationBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final MemberRepository memberRepository;

    @Transactional
    public void setInsert (OperationBoardCommentDTO paramDTO, UserDetails userDetails) {
        OperationBoardComment operationBoardComment = new OperationBoardComment();
        Optional<OperationBoard> optionOperationBoard = operationBoardRepository.findById(paramDTO.getOperationBoardId());
        OperationBoard operationBoard = null;
        if (optionOperationBoard.isPresent()) {
            operationBoard = optionOperationBoard.get();
            operationBoardComment.setOperationBoard(operationBoard);
            operationBoardComment.setContent(paramDTO.getContent());
            operationBoardComment.setMember(memberService.view(paramDTO.getMemberId()));

            Member member=memberRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원 없음"));

            //새로운 댓글 엔티티 객체 생성해서 담은후 댓글 db저장
            int ref = operationBoardCommentRepository.getMaxRef()+1;
            operationBoardComment.setContent(paramDTO.getContent());
            operationBoardComment.setOperationBoard(operationBoard);
            operationBoardComment.setRef(ref); //새댓글 +1
            operationBoardComment.setStep(0);   //새댓글 기본값 0
            operationBoardComment.setLevel(0);  //새댓글 기본값 0
            operationBoardComment.setMember(member);
            operationBoardCommentRepository.save(operationBoardComment);
            operationBoardCommentRepository.save(operationBoardComment);
        }
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

    //대댓글 추가
    @Transactional
    public void replysetInsert(
            OperationBoardCommentDTO paramDTO,
            UserDetails userDetails
    ){
        OperationBoardComment operationBoardComment_ = operationBoardCommentRepository.findById(paramDTO.getOperationBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 없음"));

        operationBoardCommentRepository.updateRelevel(
                operationBoardComment_.getRef(),
                operationBoardComment_.getLevel()
        );

        int ref=operationBoardComment_.getRef();
        int step=operationBoardComment_.getStep()+1;
        int level=operationBoardComment_.getLevel()+1;

        OperationBoard operationBoard=null;
        Optional<OperationBoard> operationBoardOptional=operationBoardRepository.findById(paramDTO.getOperationBoardId());
        if(operationBoardOptional.isPresent()){
            operationBoard=operationBoardOptional.get(); //부모글 존재유무판단
        }


        OperationBoardComment operationBoardComment=new OperationBoardComment();
        operationBoardComment.setContent(paramDTO.getContent());
        operationBoardComment.setOperationBoard(operationBoard);
        operationBoardComment.setMember(member);
        operationBoardComment.setRef(ref);
        operationBoardComment.setStep(step);
        operationBoardComment.setLevel(level);

        operationBoardCommentRepository.save(operationBoardComment);
    }
    
}

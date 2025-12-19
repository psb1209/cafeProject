package com.example.cafeProject.operationBoardComment;

import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.operationBoard.OperationBoard;
import com.example.cafeProject.operationBoard.OperationBoardDTO;
import com.example.cafeProject.operationBoard.OperationBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public void setInsert (OperationBoardCommentDTO paramDTO) {
        OperationBoardComment operationBoardComment = new OperationBoardComment();
        Optional<OperationBoard> optionOperationBoard = operationBoardRepository.findById(paramDTO.getOperationBoardId());
        OperationBoard operationBoard = null;
        if (optionOperationBoard.isPresent()) {
            operationBoard = optionOperationBoard.get();
            operationBoardComment.setOperationBoard(operationBoard);
            operationBoardComment.setContent(paramDTO.getContent());
            operationBoardComment.setMember(memberService.view(paramDTO.getMemberId()));

            operationBoardCommentRepository.save(operationBoardComment);
        }
    }

    @Transactional
    public Page<OperationBoardComment> getCommentListPage(int operationBoardId, Pageable pageable) {
        return operationBoardCommentRepository.findByOperationBoardId(operationBoardId, pageable);
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

}

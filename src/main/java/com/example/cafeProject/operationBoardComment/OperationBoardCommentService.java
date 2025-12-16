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
    public void setInsert (OperationBoardCommentDTO operationBoardCommentDTO) {
        OperationBoardComment operationBoardComment = new OperationBoardComment();
        Optional<OperationBoard> optionOperationBoard = operationBoardRepository.findById(operationBoardCommentDTO.getOperationBoardId());
        OperationBoard operationBoard = null;
        if (optionOperationBoard.isPresent()) {
            operationBoard = optionOperationBoard.get();
            operationBoardComment.setOperationBoard(operationBoard);
            operationBoardComment.setContent(operationBoardCommentDTO.getContent());
            operationBoardComment.setMember(memberService.view(operationBoardCommentDTO.getMemberId()));

            operationBoardCommentRepository.save(operationBoardComment);
        }
    }

    @Transactional
    public Page<OperationBoardComment> getCommentListPage(int operationBoardId, Pageable pageable) {
        return operationBoardCommentRepository.findByOperationBoardId(operationBoardId, pageable);
    }

    @Transactional
    public void setDelete(OperationBoardCommentDTO operationBoardCommentDTO) {
        OperationBoardComment operationBoardComment = operationBoardCommentRepository.findById(operationBoardCommentDTO.getOperationBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 답변을 찾을 수 없습니다."));

        operationBoardCommentRepository.delete(operationBoardComment);
    }

    @Transactional
    public void setDeleteAll(OperationBoardDTO operationBoardDTO) {
        List<OperationBoardComment> operationBoardComment = operationBoardCommentRepository.findByOperationBoardId(operationBoardDTO.getId());

        operationBoardCommentRepository.deleteAll(operationBoardComment);
    }

}

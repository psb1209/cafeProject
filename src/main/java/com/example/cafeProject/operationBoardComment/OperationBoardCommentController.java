package com.example.cafeProject.operationBoardComment;

import com.example.cafeProject.operationBoard.OperationBoard;
import com.example.cafeProject.operationBoard.OperationBoardDTO;
import com.example.cafeProject.operationBoard.OperationBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/operationBoardComment")
@Controller
public class OperationBoardCommentController {

    private final OperationBoardService operationBoardService;
    private final OperationBoardCommentService operationBoardCommentService;


    String dirName = "operationBoard";

    @PostMapping("/createProc")
    public String createProc(
            Model model,
            OperationBoardCommentDTO operationBoardCommentDTO
    ) {
        try {
            OperationBoardDTO operationBoardDTO = new OperationBoardDTO();
            operationBoardDTO.setId(operationBoardCommentDTO.getOperationBoardId());
            operationBoardService.getSelectOneById(operationBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err0101");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            operationBoardCommentService.setInsert(operationBoardCommentDTO);
            return "redirect:/" + dirName + "/view/" + operationBoardCommentDTO.getOperationBoardId();
        } catch (Exception e) {
            model.addAttribute("errCode", "err0909");
            model.addAttribute("errMsg", "댓글 등록 중 오류 발생");
            return "error/error";
        }
    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            Model model,
            OperationBoardCommentDTO operationBoardCommentDTO
    ) {
        try {
            OperationBoardDTO operationBoardDTO = new OperationBoardDTO();
            operationBoardDTO.setId(operationBoardCommentDTO.getOperationBoardId());
            operationBoardService.getSelectOneById(operationBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err0101");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            operationBoardCommentService.setDelete(operationBoardCommentDTO);
            return "redirect:/" + dirName + "/view/" + operationBoardCommentDTO.getOperationBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err0001");
            model.addAttribute("errMsg", e.getMessage()); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        } catch (Exception e) { // 모든 예외는 Exception 상속 받기에 Exception이 나중에 처리되어야 함
            model.addAttribute("errCode", "err0003");
            model.addAttribute("errMsg", "삭제 처리 중 오류가 발생했습니다."); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        }

    }
 }

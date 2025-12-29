package com.example.cafeProject.operationBoardComment;

import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import com.example.cafeProject.operationBoardComment.OperationBoardCommentDTO;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.operationBoard.OperationBoard;
import com.example.cafeProject.operationBoard.OperationBoardDTO;
import com.example.cafeProject.operationBoard.OperationBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            OperationBoardCommentDTO operationBoardCommentDTO,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String loginId = userDetails.getUsername(); // 로그인했을 때 아이디
        try {
            Member member = operationBoardService.getSelectOneByUsername(authentication);
            operationBoardCommentDTO.setMemberId(member.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1110");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            OperationBoardDTO operationBoardDTO = new OperationBoardDTO();
            operationBoardDTO.setId(operationBoardCommentDTO.getOperationBoardId());
            operationBoardService.getSelectOneById(operationBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            operationBoardCommentService.setInsert(operationBoardCommentDTO, userDetails);
            return "redirect:/" + dirName + "/view/" + operationBoardCommentDTO.getOperationBoardId();
        } catch (Exception e) {
            model.addAttribute("errCode", "err2232");
            model.addAttribute("errMsg", "등록 중 문제가 발생했습니다.");
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
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            operationBoardCommentService.setDelete(operationBoardCommentDTO);
            return "redirect:/" + dirName + "/view/" + operationBoardCommentDTO.getOperationBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage()); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        } catch (Exception e) { // 모든 예외는 Exception 상속 받기에 Exception이 나중에 처리되어야 함
            model.addAttribute("errCode", "err2242");
            model.addAttribute("errMsg", "삭제 중 문제가 발생했습니다."); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        }
    }

    @PostMapping("/update")
    public String update(
            OperationBoardCommentDTO operationBoardCommentDTO,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            OperationBoardComment operationBoardCommentUpdate = operationBoardCommentService.getOperationBoardCommentId(operationBoardCommentDTO);
            redirectAttributes.addFlashAttribute("operationBoardCommentUpdate", operationBoardCommentUpdate);
            return "redirect:/operationBoard/view/" + operationBoardCommentDTO.getOperationBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }


    @PostMapping("/updateProc")
    public String updateProc(
            Model model,
            OperationBoardCommentDTO operationBoardCommentDTO
    ) {
        try {
            OperationBoardDTO operationBoardDTO = new OperationBoardDTO();
            operationBoardDTO.setId(operationBoardCommentDTO.getOperationBoardId());
            operationBoardService.getSelectOneById(operationBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
        
        try {
            operationBoardCommentService.setUpdate(operationBoardCommentDTO);
            return "redirect:/" + dirName + "/view/" + operationBoardCommentDTO.getOperationBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage()); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "err2242");
            model.addAttribute("errMsg", "수정 중 문제가 발생했습니다."); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        }
    }

    //대댓글 추가
    @GetMapping("/replyComment/{operationBoardCommentId}")
    public String replyComment(
            @PathVariable int operationBoardCommentId,
            OperationBoardCommentDTO operationBoardCommentDTO,
            Model model,
            Authentication authentication
    ) {
        OperationBoardComment operationBoardComment = operationBoardCommentService.getOperationBoardCommentId(operationBoardCommentDTO);
        model.addAttribute("operationBoardComment",operationBoardComment);
        return "operationBoard/replyComment";
    }


    //대댓글 추가처리
    @PostMapping("/replyCreateProc")
    public String replyCreateProc(
            OperationBoardCommentDTO operationBoardCommentDTO,
            Model model,
            Authentication authentication
    ){
        try {
            UserDetails userDetails=(UserDetails)authentication.getPrincipal();

            operationBoardCommentService.replysetInsert(operationBoardCommentDTO, userDetails);
            return "redirect:/operationBoard/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "221213321");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "에러0005");
            model.addAttribute("errMsg", "해당 댓글이 해당하지 않습니다.");
            return "error/error";
        }
    }
 }

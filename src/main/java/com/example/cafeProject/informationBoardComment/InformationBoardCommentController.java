package com.example.cafeProject.informationBoardComment;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/informationBoardComment")
@RequiredArgsConstructor
@Controller
public class InformationBoardCommentController {

    private final InformationBoardCommentService informationBoardCommentService;


    @PostMapping("/createProc")
    public String createProc(Model model, InformationBoardCommentDTO informationBoardCommentDTO, @AuthenticationPrincipal User user) {
        try {
            informationBoardCommentService.setInsert(informationBoardCommentDTO, user);
            return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "에러 발생");
            return "error/error";
        }
    }


    @PostMapping("/deleteProc")
    public String deleteProc(Model model, InformationBoardCommentDTO informationBoardCommentDTO, @AuthenticationPrincipal User user) {
        try {
            informationBoardCommentService.setDelete(informationBoardCommentDTO, user);
            return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 댓글을 찾을 수 없습니다.");
            return "error/error";
        }
    }



}

package com.example.cafeProject.informationBoardComment;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/informationBoardComment")
@RequiredArgsConstructor
@Controller
public class InformationBoardCommentController {

    private final InformationBoardCommentService informationBoardCommentService;


    @PostMapping("/createProc")
    public String createProc(Model model, InformationBoardCommentDTO informationBoardCommentDTO,
                             @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        try {
            boolean isUpgraded = informationBoardCommentService.setInsert(informationBoardCommentDTO, user);
            if(isUpgraded) { //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉"); //윈도우 로고 키(⊞) + 마침표(.) --> 임티창
                return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId();
            }
            return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId(); //등급 안올랐으면 걍 조용히 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 댓글을 찾을 수 없습니다.");
            return "error/error";
        }
    }


    @PostMapping("/deleteProc")
    public String deleteProc(Model model, InformationBoardCommentDTO informationBoardCommentDTO, @AuthenticationPrincipal User user) {
        try {
            informationBoardCommentService.setDelete(informationBoardCommentDTO, user);
            return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId();
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 댓글을 찾을 수 없습니다.");
            return "error/error";
        }
    }



}

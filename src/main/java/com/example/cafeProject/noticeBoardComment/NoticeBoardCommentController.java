package com.example.cafeProject.noticeBoardComment;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoard.NoticeBoardDTO;
import com.example.cafeProject.noticeBoard.NoticeBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@RequestMapping("/noticeBoardComment")
@Controller
public class NoticeBoardCommentController {

    private final NoticeBoardService noticeBoardService;
    private final NoticeBoardCommentService noticeBoardCommentService;
    private final MemberService memberService;


    String dirName = "noticeBoard";

    @PostMapping("/createProc")
    public String createProc(
            Model model,
            NoticeBoardCommentDTO noticeBoardCommentDTO,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal User user
    ) {
        try {
            Member member = memberService.viewCurrentMember(authentication);
            noticeBoardCommentDTO.setMemberId(member.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1110");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            NoticeBoardDTO noticeBoardDTO = new NoticeBoardDTO();
            noticeBoardDTO.setId(noticeBoardCommentDTO.getNoticeBoardId());
            noticeBoardService.getSelectOneById(noticeBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            boolean isUpgraded = noticeBoardCommentService.setInsert(noticeBoardCommentDTO, user);
            if(isUpgraded) { //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉"); //윈도우 로고 키(⊞) + 마침표(.) --> 임티창
                return "redirect:/noticeBoard/view/" + noticeBoardCommentDTO.getNoticeBoardId();
            }
            return "redirect:/noticeBoard/view/" + noticeBoardCommentDTO.getNoticeBoardId(); //등급 안올랐으면 걍 조용히 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 댓글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            Model model,
            NoticeBoardCommentDTO noticeBoardCommentDTO
    ) {

        try {
            NoticeBoardDTO noticeBoardDTO = new NoticeBoardDTO();
            noticeBoardDTO.setId(noticeBoardCommentDTO.getNoticeBoardId());
            noticeBoardService.getSelectOneById(noticeBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            noticeBoardCommentService.setDelete(noticeBoardCommentDTO);
            return "redirect:/" + dirName + "/view/" + noticeBoardCommentDTO.getNoticeBoardId();
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
            NoticeBoardCommentDTO noticeBoardCommentDTO,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            NoticeBoardComment noticeBoardCommentUpdate = noticeBoardCommentService.getNoticeBoardCommentId(noticeBoardCommentDTO);
            redirectAttributes.addFlashAttribute("noticeBoardCommentUpdate", noticeBoardCommentUpdate);
            return "redirect:/noticeBoard/view/" + noticeBoardCommentDTO.getNoticeBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }


    @PostMapping("/updateProc")
    public String updateProc(
            Model model,
            NoticeBoardCommentDTO noticeBoardCommentDTO
    ) {
        try {
            NoticeBoardDTO noticeBoardDTO = new NoticeBoardDTO();
            noticeBoardDTO.setId(noticeBoardCommentDTO.getNoticeBoardId());
            noticeBoardService.getSelectOneById(noticeBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
        
        try {
            noticeBoardCommentService.setUpdate(noticeBoardCommentDTO);
            return "redirect:/" + dirName + "/view/" + noticeBoardCommentDTO.getNoticeBoardId();
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
/*
    @GetMapping("/replyComment/{noticeBoardCommentId}")
    public String replyComment(
            @PathVariable int noticeBoardCommentId,
            NoticeBoardCommentDTO noticeBoardCommentDTO,
            Model model,
            Authentication authentication
    ) {
        NoticeBoardComment noticeBoardComment = noticeBoardCommentService.getNoticeBoardCommentId(noticeBoardCommentDTO);
        model.addAttribute("noticeBoardComment",noticeBoardComment);
        return "noticeBoard/replyComment";
    }
*/


    //대댓글 추가처리
    @PostMapping("/replyProc")
    public String replyProc(
            NoticeBoardCommentDTO noticeBoardCommentDTO,
            Model model,
            Authentication authentication
    ){
        try {
            UserDetails userDetails=(UserDetails)authentication.getPrincipal();

            noticeBoardCommentService.replysetInsert(noticeBoardCommentDTO, userDetails);
            return "redirect:/noticeBoard/view/" + noticeBoardCommentDTO.getNoticeBoardId();
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
